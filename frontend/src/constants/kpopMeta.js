export const CARD_TYPES = [
  '专辑随机卡', '专辑固配卡', '特典卡(POB)', '幸运卡(Lucky Draw)',
  '签售卡(Fansign)', '视频签售卡', '打歌/Broadcast卡', '周边卡', "Season's Greetings", '其他',
];

export const QUALITIES = ['无暇', '微瑕', '重瑕'];
export const TRADE_TYPES = ['仅出售', '可交换', '支持预留'];

export const GROUP_MEMBERS = {
  BTS: ['RM', 'Jin', 'SUGA', 'j-hope', 'Jimin', 'V', 'Jung Kook'],
  SEVENTEEN: ['S.Coups', 'Jeonghan', 'Joshua', 'Jun', 'Hoshi', 'Wonwoo', 'Woozi', 'DK', 'Mingyu', 'The8', 'Seungkwan', 'Vernon', 'Dino'],
  NCT: ['Taeyong', 'Johnny', 'Yuta', 'Kun', 'Doyoung', 'Ten', 'Jaehyun', 'Winwin', 'Jungwoo', 'Mark', 'Xiaojun', 'Hendery', 'Renjun', 'Jeno', 'Haechan', 'Jaemin', 'Yangyang', 'Shotaro', 'Sungchan', 'Chenle', 'Jisung'],
  'Stray Kids': ['Bang Chan', 'Lee Know', 'Changbin', 'Hyunjin', 'Han', 'Felix', 'Seungmin', 'I.N'],
  aespa: ['Karina', 'Giselle', 'Winter', 'Ningning'],
  NewJeans: ['Minji', 'Hanni', 'Danielle', 'Haerin', 'Hyein'],
  IVE: ['Yujin', 'Gaeul', 'Rei', 'Wonyoung', 'Liz', 'Leeseo'],
  'LE SSERAFIM': ['Sakura', 'Chaewon', 'Yunjin', 'Kazuha', 'Eunchae'],
  TWICE: ['Nayeon', 'Jeongyeon', 'Momo', 'Sana', 'Jihyo', 'Mina', 'Dahyun', 'Chaeyoung', 'Tzuyu'],
  BLACKPINK: ['Jisoo', 'Jennie', 'Rosé', 'Lisa'],
  EXO: ['Suho', 'Xiumin', 'Lay', 'Baekhyun', 'Chen', 'Chanyeol', 'D.O.', 'Kai', 'Sehun'],
  ENHYPEN: ['Heeseung', 'Jay', 'Jake', 'Sunghoon', 'Sunoo', 'Jungwon', 'Ni-ki'],
};

export const GROUP_NAMES = Object.keys(GROUP_MEMBERS);

export async function loadKpopMeta(axios) {
  try {
    const res = await axios.get('/api/meta/kpop');
    if (res.data?.code === 0 && res.data.data) {
      return res.data.data;
    }
  } catch (_) { /* fallback */ }
  return { groups: GROUP_MEMBERS, cardTypes: CARD_TYPES, qualities: QUALITIES, tradeTypes: TRADE_TYPES };
}
