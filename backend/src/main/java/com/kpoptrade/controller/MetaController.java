package com.kpoptrade.controller;

import com.kpoptrade.util.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    @GetMapping("/kpop")
    public R<Map<String, Object>> kpopMeta() {
        Map<String, Object> meta = new LinkedHashMap<>();

        meta.put("agencies", Arrays.asList("HYBE", "SM", "JYP", "YG", "CUBE", "Starship", "其他"));

        Map<String, List<String>> groups = new LinkedHashMap<>();
        groups.put("BTS", Arrays.asList("RM", "Jin", "SUGA", "j-hope", "Jimin", "V", "Jung Kook"));
        groups.put("SEVENTEEN", Arrays.asList("S.Coups", "Jeonghan", "Joshua", "Jun", "Hoshi", "Wonwoo", "Woozi", "DK", "Mingyu", "The8", "Seungkwan", "Vernon", "Dino"));
        groups.put("NCT", Arrays.asList("Taeyong", "Johnny", "Yuta", "Kun", "Doyoung", "Ten", "Jaehyun", "Winwin", "Jungwoo", "Mark", "Xiaojun", "Hendery", "Renjun", "Jeno", "Haechan", "Jaemin", "Yangyang", "Shotaro", "Sungchan", "Chenle", "Jisung"));
        groups.put("Stray Kids", Arrays.asList("Bang Chan", "Lee Know", "Changbin", "Hyunjin", "Han", "Felix", "Seungmin", "I.N"));
        groups.put("aespa", Arrays.asList("Karina", "Giselle", "Winter", "Ningning"));
        groups.put("NewJeans", Arrays.asList("Minji", "Hanni", "Danielle", "Haerin", "Hyein"));
        groups.put("IVE", Arrays.asList("Yujin", "Gaeul", "Rei", "Wonyoung", "Liz", "Leeseo"));
        groups.put("LE SSERAFIM", Arrays.asList("Sakura", "Chaewon", "Yunjin", "Kazuha", "Eunchae"));
        groups.put("TWICE", Arrays.asList("Nayeon", "Jeongyeon", "Momo", "Sana", "Jihyo", "Mina", "Dahyun", "Chaeyoung", "Tzuyu"));
        groups.put("BLACKPINK", Arrays.asList("Jisoo", "Jennie", "Rosé", "Lisa"));
        groups.put("EXO", Arrays.asList("Suho", "Xiumin", "Lay", "Baekhyun", "Chen", "Chanyeol", "D.O.", "Kai", "Sehun"));
        groups.put("ENHYPEN", Arrays.asList("Heeseung", "Jay", "Jake", "Sunghoon", "Sunoo", "Jungwon", "Ni-ki"));
        meta.put("groups", groups);

        meta.put("cardTypes", Arrays.asList(
                "专辑随机卡", "专辑固配卡", "特典卡(POB)", "幸运卡(Lucky Draw)",
                "签售卡(Fansign)", "视频签售卡", "打歌/Broadcast卡", "周边卡", "Season's Greetings", "其他"
        ));

        meta.put("qualities", Arrays.asList("无暇", "微瑕", "重瑕"));
        meta.put("tradeTypes", Arrays.asList("仅出售", "可交换", "支持预留"));
        meta.put("deliveryModes", Arrays.asList("普通邮寄", "校园同城面交"));

        return R.ok(meta);
    }

    @GetMapping("/express")
    public R<List<String>> expressCompanies() {
        return R.ok(Arrays.asList(
                "顺丰速运", "圆通速递", "中通快递", "韵达快递", "申通快递",
                "京东物流", "邮政EMS", "极兔速递", "德邦快递", "校园面交", "其他"
        ));
    }
}
