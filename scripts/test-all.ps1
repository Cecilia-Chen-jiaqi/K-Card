# K-CARD 全功能 API 自动化测试
$Base = "http://localhost:8080"
$ts = Get-Date -Format "yyyyMMddHHmmss"
$results = @()

function Test-Step($name, $scriptBlock) {
    try {
        $r = & $scriptBlock
        if ($r.ok) {
            $results += [PSCustomObject]@{ Test = $name; Status = "PASS"; Detail = $r.detail }
            Write-Host "[PASS] $name - $($r.detail)" -ForegroundColor Green
        } else {
            $results += [PSCustomObject]@{ Test = $name; Status = "FAIL"; Detail = $r.detail }
            Write-Host "[FAIL] $name - $($r.detail)" -ForegroundColor Red
        }
        return $r
    } catch {
        $results += [PSCustomObject]@{ Test = $name; Status = "FAIL"; Detail = $_.Exception.Message }
        Write-Host "[FAIL] $name - $($_.Exception.Message)" -ForegroundColor Red
        return @{ ok = $false }
    }
}

function Invoke-Api($Method, $Uri, $Body = $null, $Token = $null) {
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{ Uri = $Uri; Method = $Method; Headers = $headers }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Depth 5) }
    return Invoke-RestMethod @params
}

Write-Host "`n=== K-CARD 功能测试开始 ===`n" -ForegroundColor Cyan

# 1. K-pop 元数据
Test-Step "K-pop元数据 /api/meta/kpop" {
    $res = Invoke-Api GET "$Base/api/meta/kpop"
    if ($res.code -eq 0 -and $res.data.groups) { @{ ok = $true; detail = "团体数: $(($res.data.groups | Get-Member -MemberType NoteProperty).Count)" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 2. 注册卖家
$sellerUser = "seller_$ts"
$sellerPhone = "138" + (Get-Random -Minimum 10000000 -Maximum 99999999)
$sellerRes = Test-Step "注册卖家" {
    $res = Invoke-Api POST "$Base/api/auth/register" @{
        username = $sellerUser; password = "123456"; phone = $sellerPhone; campus = "测试大学"
    }
    if ($res.code -eq 0) { @{ ok = $true; detail = $sellerUser } }
    else { @{ ok = $false; detail = $res.message } }
}

# 3. 注册买家
$buyerUser = "buyer_$ts"
$buyerPhone = "139" + (Get-Random -Minimum 10000000 -Maximum 99999999)
$buyerRes = Test-Step "注册买家" {
    $res = Invoke-Api POST "$Base/api/auth/register" @{
        username = $buyerUser; password = "123456"; phone = $buyerPhone; campus = "测试大学"
    }
    if ($res.code -eq 0) { @{ ok = $true; detail = $buyerUser } }
    else { @{ ok = $false; detail = $res.message } }
}

# 4. 登录
$sellerToken = $null
$buyerToken = $null
Test-Step "卖家登录" {
    $res = Invoke-Api POST "$Base/api/auth/login" @{ username = $sellerUser; password = "123456" }
    if ($res.code -eq 0) { $script:sellerToken = $res.data.token; @{ ok = $true; detail = "token获取成功" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "买家登录" {
    $res = Invoke-Api POST "$Base/api/auth/login" @{ username = $buyerUser; password = "123456" }
    if ($res.code -eq 0) { $script:buyerToken = $res.data.token; @{ ok = $true; detail = "token获取成功" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 5. 图片上传
$imageUrl = $null
Test-Step "图片上传 /api/upload/image" {
    $pngBytes = [byte[]](0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A,0x00,0x00,0x00,0x0D,0x49,0x48,0x44,0x52,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x01,0x08,0x02,0x00,0x00,0x00,0x90,0x77,0x53,0xDE,0x00,0x00,0x00,0x0C,0x49,0x44,0x41,0x54,0x08,0xD7,0x63,0xF8,0xCF,0xC0,0x00,0x00,0x03,0x01,0x01,0x00,0x18,0xDD,0x8D,0xB4,0x00,0x00,0x00,0x00,0x49,0x45,0x4E,0x44,0xAE,0x42,0x60,0x82)
    $tmpFile = [System.IO.Path]::GetTempFileName() + ".png"
    [System.IO.File]::WriteAllBytes($tmpFile, $pngBytes)
    $boundary = [System.Guid]::NewGuid().ToString()
    $fileContent = [System.IO.File]::ReadAllBytes($tmpFile)
    $enc = [System.Text.Encoding]::UTF8
    $bodyLines = @(
        "--$boundary",
        'Content-Disposition: form-data; name="file"; filename="test.png"',
        'Content-Type: image/png',
        '',
        ''
    )
    $bodyStart = $enc.GetBytes(($bodyLines -join "`r`n"))
    $bodyEnd = $enc.GetBytes("`r`n--$boundary--`r`n")
    $body = New-Object byte[] ($bodyStart.Length + $fileContent.Length + $bodyEnd.Length)
    [Array]::Copy($bodyStart, 0, $body, 0, $bodyStart.Length)
    [Array]::Copy($fileContent, 0, $body, $bodyStart.Length, $fileContent.Length)
    [Array]::Copy($bodyEnd, 0, $body, $bodyStart.Length + $fileContent.Length, $bodyEnd.Length)
    $res = Invoke-RestMethod -Uri "$Base/api/upload/image" -Method POST -ContentType "multipart/form-data; boundary=$boundary" -Body $body
    Remove-Item $tmpFile -Force -ErrorAction SilentlyContinue
    if ($res.code -eq 0) { $script:imageUrl = $res.data; @{ ok = $true; detail = $res.data } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 6. 发布商品
$goodsId = $null
Test-Step "发布小卡 /api/goods/create" {
    $res = Invoke-Api POST "$Base/api/goods/create" @{
        title = "测试小卡 $ts"; description = "自动化测试"; price = 0.01; stock = 5
        groupName = "SEVENTEEN"; idolName = "Mingyu"; cardType = "专辑随机卡"; albumEra = "FML"
        quality = "无暇"; tradeType = "仅出售"; deliveryMode = 1; coverImage = $imageUrl
    } $sellerToken
    if ($res.code -eq 0) { $script:goodsId = $res.data.id; @{ ok = $true; detail = "goodsId=$($res.data.id)" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 7. 商品列表与搜索
Test-Step "商品列表 /api/goods/list" {
    $res = Invoke-Api GET "$Base/api/goods/list"
    if ($res.code -eq 0) { @{ ok = $true; detail = "共 $($res.data.Count) 件" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "商品搜索 /api/goods/search" {
    $res = Invoke-RestMethod -Uri "$Base/api/goods/search?groupName=SEVENTEEN&idolName=Mingyu" -Method GET
    if ($res.code -eq 0) { @{ ok = $true; detail = "匹配 $($res.data.Count) 件" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "商品详情 /api/goods/detail" {
    $res = Invoke-RestMethod -Uri "$Base/api/goods/detail?id=$goodsId" -Method GET
    if ($res.code -eq 0 -and $res.data.title) { @{ ok = $true; detail = $res.data.title } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "我的发布 /api/goods/my" {
    $res = Invoke-Api GET "$Base/api/goods/my" $null $sellerToken
    if ($res.code -eq 0) { @{ ok = $true; detail = "共 $($res.data.Count) 件" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 8. 购物车
$cartId = $null
Test-Step "加入购物车 /api/cart/add" {
    $res = Invoke-Api POST "$Base/api/cart/add" @{ goodsId = $goodsId; quantity = 1 } $buyerToken
    if ($res.code -eq 0) { @{ ok = $true; detail = "加购成功" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "购物车列表 /api/cart/list" {
    $res = Invoke-Api GET "$Base/api/cart/list" $null $buyerToken
    if ($res.code -eq 0 -and $res.data.Count -gt 0) {
        $script:cartId = $res.data[0].cart.id
        @{ ok = $true; detail = "cartId=$cartId" }
    } else { @{ ok = $false; detail = "购物车为空" } }
} | Out-Null

# 9. 直接下单
$orderNo = $null
Test-Step "创建订单 /api/orders/create" {
    $res = Invoke-Api POST "$Base/api/orders/create" @{ goodsId = $goodsId; quantity = 1; payType = 1 } $buyerToken
    if ($res.code -eq 0) { $script:orderNo = $res.data.orderNo; @{ ok = $true; detail = $orderNo } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "订单详情 /api/orders/{orderNo}" {
    $res = Invoke-Api GET "$Base/api/orders/$orderNo" $null $buyerToken
    if ($res.code -eq 0 -and $res.data.order.status -eq 0) { @{ ok = $true; detail = "待支付" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "买家订单列表" {
    $res = Invoke-Api GET "$Base/api/orders/buyer/list" $null $buyerToken
    if ($res.code -eq 0) { @{ ok = $true; detail = "共 $($res.data.Count) 笔" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "卖家订单列表" {
    $res = Invoke-Api GET "$Base/api/orders/seller/list" $null $sellerToken
    if ($res.code -eq 0) { @{ ok = $true; detail = "共 $($res.data.Count) 笔" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 10. 支付
Test-Step "支付宝表单生成 /api/pay/submit" {
    $res = Invoke-Api POST "$Base/api/pay/submit" @{ orderNo = $orderNo } $buyerToken
    if ($res.code -eq 0 -and $res.data -like "*form*") { @{ ok = $true; detail = "支付表单已生成" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "支付状态同步 /api/pay/sync-status" {
    $res = Invoke-RestMethod -Uri "$Base/api/pay/sync-status?orderNo=$orderNo" -Method GET
    if ($res.code -eq 0) { @{ ok = $true; detail = "paid=$($res.data.paid), status=$($res.data.localStatus)" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 11. 取消订单（新建一笔再取消）
Test-Step "取消订单 /api/orders/cancel" {
    $create = Invoke-Api POST "$Base/api/orders/create" @{ goodsId = $goodsId; quantity = 1; payType = 1 } $buyerToken
    if ($create.code -ne 0) { return @{ ok = $false; detail = "无法创建测试订单: $($create.message)" } }
    $cancelNo = $create.data.orderNo
    $res = Invoke-Api POST "$Base/api/orders/cancel" @{ orderNo = $cancelNo } $buyerToken
    if ($res.code -eq 0) { @{ ok = $true; detail = "已取消 $cancelNo" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 12. 校园/换卡专区
Test-Step "校园专区 /api/goods/campus" {
    $res = Invoke-Api GET "$Base/api/goods/campus"
    if ($res.code -eq 0) { @{ ok = $true; detail = "共 $($res.data.Count) 件" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

Test-Step "换卡专区 /api/goods/exchange" {
    $res = Invoke-Api GET "$Base/api/goods/exchange"
    if ($res.code -eq 0) { @{ ok = $true; detail = "共 $($res.data.Count) 件" } }
    else { @{ ok = $false; detail = $res.message } }
} | Out-Null

# 汇总
Write-Host "`n=== 测试汇总 ===" -ForegroundColor Cyan
$pass = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$fail = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "通过: $pass / $($results.Count)  失败: $fail"
$results | Format-Table -AutoSize
if ($fail -gt 0) { exit 1 }
