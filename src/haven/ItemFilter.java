package haven;

import haven.res.ui.tt.armor.Armor;
import haven.res.ui.tt.attrmod.Mod;
import haven.res.ui.tt.q.qbuff.QBuff;
import haven.resutil.Curiosity;
import haven.resutil.FoodInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ND: Ported from Cediner/ArdClient (archived). Query-based item/recipe filter used by
 * the Craft Database window. Adapted to Hurricane's ItemInfo subclasses. */
public class ItemFilter {
    private static final Pattern q = Pattern.compile("(?:(\\w+))?(?:^|:)([\\w*]+)?(?:([<>=+~])(\\d+(?:\\.\\d+)?)?([<>=+~])?)?", Pattern.UNICODE_CHARACTER_CLASS);

    public static final String HELP_SIMPLE = "$size[20]{$b{Simple search}}\n" +
            "Just enter text and items with matching names will get highlighted\n";

    public static final String HELP_FULL_TEXT = "$size[20]{$b{Full text search}}\n" +
            "$font[monospaced,16]{txt:[text]}\n" +
            "Will search for $font[monospaced,13]{[text]} in the item's name.\n";

    public static final String HELP_CONTENT = "$size[20]{$b{Contents search}}\n" +
            "$font[monospaced,16]{has:[txt][sign][value]}\n" +
            "Will highlight all items that have $font[monospaced,13]{[txt]} in their contents in quantity specified by $font[monospaced,13]{[sign]} and $font[monospaced,13]{[value]}.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  has:water    }will find items that have water in their Contents.\n" +
            "$font[monospaced,13]{  has:water>2  }will find items that contain more than 2L of water.\n" +
            "$font[monospaced,13]{  has:water+3  }will find items that contain at least 3L of water.\n" +
            "$font[monospaced,13]{  has:water<10 }will find items that contain less than 10L of water.\n" +
            "$font[monospaced,13]{  has:water=2  }will find items that contain exactly 2L of water.\n";

    public static final String HELP_QUALITY = "$size[20]{$b{Quality search}}\n" +
            "$font[monospaced,16]{q:[type][sign][value][opt]}\n" +
            "Will highlight items with quality type defined by $font[monospaced,13]{[type]} or $font[monospaced,13]{[opt]} with quality value specified by $font[monospaced,13]{[sign]} and $font[monospaced,13]{[value]}.\n" +
            "$font[monospaced,13]{[type]} is type of quality ($font[monospaced,13]{min, max, average, essence, vitality, substance}). You can write type not fully ($font[monospaced,13]{ess} will match essence, for example). If you omit type, it will be detected by $font[monospaced,13]{[opt]} (> means max, < means min, = and ~ means average).\n" +
            "$font[monospaced,13]{[sign]} can be $font[monospaced,13]{>} (more), $font[monospaced,13]{+} (at least), $font[monospaced,13]{<} (less), $font[monospaced,13]{=} (exactly).\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  q:>5     }will find items with default quality higher than 5\n" +
            "$font[monospaced,13]{  q:min<12 }will find items with minimum quality less than 12\n" +
            "$font[monospaced,13]{  q:ess+21 }will find items with essence of at least 21\n";

    public static final String HELP_CURIO = "$size[20]{$b{Curiosity search}}\n" +
            "Supports $font[monospaced,13]{lp} (learning point gained), $font[monospaced,13]{xp} (experience required) and $font[monospaced,13]{mw} (mental weight required) tags.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  lp:    }will find items that grant LP.\n" +
            "$font[monospaced,13]{  lp>100 }will find items that grant more than 100 LP.\n" +
            "$font[monospaced,13]{  lp+200 }will find items that grant at least 200 LP.\n" +
            "$font[monospaced,13]{  lp<300 }will find items that grant no more than 300 LP.\n";

    public static final String HELP_FEP = "$size[20]{$b{FEP search}}\n" +
            "$font[monospaced,16]{fep:[type][sign][value]}\n" +
            "Will highlight items that grant FEPs of type $font[monospaced,13]{[type]} in quantity described by $font[monospaced,13]{[sign]} and $font[monospaced,13]{[value]}.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  fep:str>1 }will find food giving more than 1 Strength FEPs.\n" +
            "$font[monospaced,13]{  fep:agi+2 }will find food giving at least than 2 Agility FEPs.\n" +
            "$font[monospaced,13]{  fep:cha<3 }will find food giving less than 3 Charisma FEPs.\n" +
            "$font[monospaced,13]{  fep:dex=4 }will find food giving exactly 4 Dexterity FEPs.\n" +
            "\n" +
            "$size[20]{$b{Food values search}}\n" +
            "Supports $font[monospaced,13]{hunger} or $font[monospaced,13]{hng}, (Hunger satiated) and $font[monospaced,13]{energy} or $font[monospaced,13]{nrg} (Energy restored) tags.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  nrg:>50  }will find food which restores more than 50 energy\n" +
            "$font[monospaced,13]{  nrg<120  }will find food which restores no more than 120 energy\n" +
            "$font[monospaced,13]{  hunger>2 }will find food with more than 2% hunger\n";

    public static final String HELP_ARMOR = "$size[20]{$b{Armor search}}\n" +
            "$font[monospaced,16]{armor:[type][sign][value]}\n" +
            "Use $font[monospaced,13]{hard} or $font[monospaced,13]{deflect} for hard (deflecting) armor.\n" +
            "Use $font[monospaced,13]{soft} or $font[monospaced,13]{soak} for soft (soaking) armor.\n" +
            "Use $font[monospaced,13]{all}, $font[monospaced,13]{any}, $font[monospaced,13]{total}, $font[monospaced,13]{*} or leave empty for the sum.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  armor:hard>1 }will find items providing more than 1 hard armor.\n" +
            "$font[monospaced,13]{  armor:soft<2 }will find items providing less than 2 soft armor.\n" +
            "$font[monospaced,13]{  armor:all=3  }will find items providing exactly 3 total armor.\n";

    public static final String HELP_SYMBEL = "$size[20]{$b{Symbel search}}\n" +
            "$font[monospaced,16]{symb:[type][sign][value]}\n" +
            "Use $font[monospaced,13]{fep} type to denote fep bonus.\n" +
            "Use $font[monospaced,13]{hunger} type to denote hunger modifier.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  symb:          }will find all symbel items.\n" +
            "$font[monospaced,13]{  symb:fep>2     }will find items with more than 2% fep bonus.\n" +
            "$font[monospaced,13]{  symb:hunger<3  }will find items with less than 3% hunger reduction.\n";

    public static final String HELP_ATTR = "$size[20]{$b{Attribute search}}\n" +
            "$font[monospaced,16]{attr:[type][sign][value]}\n" +
            "Will highlight items (equipment or gilding items) that grant attribute or skill bonuses defined by $font[monospaced,13]{[type]} in amount described by $font[monospaced,13]{[sign]} and $font[monospaced,13]{[value]}.\n" +
            "$font[monospaced,13]{[type]} can be any attribute/skill name and can be entered partially.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  attr:survival }will find all items that grant survival.\n" +
            "$font[monospaced,13]{  attr:str>2    }will find items granting more than 2 str bonus.\n" +
            "$font[monospaced,13]{  attr:agi<0    }will find items giving agility penalty.\n";

    public static final String HELP_INPUT = "$size[20]{$b{Craft input search}}\n" +
            "$font[monospaced,16]{from:[type][sign][value]}\n" +
            "Will highlight recipes that use input $font[monospaced,13]{[type]} in amount described by $font[monospaced,13]{[sign]} and $font[monospaced,13]{[value]}.\n" +
            "$size[16]{\nExamples:}\n" +
            "$font[monospaced,13]{  from:dream    }will find all recipes that craft from A Beautiful Dream.\n" +
            "$font[monospaced,13]{  from:board>2  }will find recipes using more than 2 boards.\n";

    public static final String[] FILTER_HELP = {HELP_SIMPLE, HELP_FULL_TEXT, HELP_CONTENT, HELP_QUALITY, HELP_CURIO, HELP_FEP, HELP_ARMOR, HELP_SYMBEL, HELP_ATTR, HELP_INPUT};

    public boolean matches(List<ItemInfo> info) {
        for (ItemInfo item : info) {
            if (match(item)) {
                return true;
            }
        }
        return false;
    }

    protected boolean match(ItemInfo item) {
        return false;
    }

    private static double round(double v, int places) {
        double f = Math.pow(10, places);
        return Math.round(v * f) / f;
    }

    // True if the query uses filter tags/operators (fep:, attr:, q:>, ...) rather than
    // being a plain name search.
    public static boolean isTagQuery(String q) {
        return q.indexOf(':') >= 0 || q.indexOf('>') >= 0 || q.indexOf('<') >= 0
                || q.indexOf('=') >= 0 || q.indexOf('+') >= 0 || q.indexOf('~') >= 0;
    }

    public static ItemFilter create(String query) {
        Compound result = new Compound();
        Matcher m = q.matcher(query);
        while (m.find()) {
            String tag = m.group(1);
            String text = m.group(2);
            String sign = m.group(3);
            String value = m.group(4);
            String opt = m.group(5);

            if (text == null) {
                text = "";
            } else {
                text = text.toLowerCase();
            }

            ItemFilter filter = null;
            if (sign != null && tag == null) {
                switch (text) {
                    case "energy":
                    case "nrg":
                        tag = text = "energy";
                        break;
                    case "hunger":
                    case "hng":
                        tag = text = "hunger";
                        break;
                    case "xp":
                    case "lp":
                    case "mw":
                        tag = text;
                        break;
                    case "q":
                        tag = "q";
                        text = "single";
                        break;
                    case "armor":
                        tag = text;
                        text = "all";
                        break;
                }
            }
            if (tag == null) {
                if (!text.isEmpty())
                    filter = new Text(text);
            } else {
                tag = tag.toLowerCase();
                switch (tag) {
                    case "txt":
                        filter = new Text(text);
                        break;
                    case "xp":
                    case "lp":
                    case "mw":
                        filter = new XP(tag, sign, value, opt);
                        break;
                    case "energy":
                    case "hunger":
                    case "fill":
                        filter = new Food(tag, sign, value, opt);
                        break;
                    case "has":
                        filter = new Has(text, sign, value, opt);
                        break;
                    case "q":
                        filter = new Q(text, sign, value, opt);
                        break;
                    case "fep":
                        filter = new FEP(text, sign, value, opt);
                        break;
                    case "armor":
                        filter = new ArmorF(text, sign, value, opt);
                        break;
                    case "gast":
                    case "symb":
                        filter = new Gastronomy(text, sign, value, opt);
                        break;
                    case "attr":
                        filter = new Attribute(text, sign, value, opt);
                        break;
                    case "ing":
                    case "from":
                        filter = new Inputs(text, sign, value, opt);
                        break;
                }
            }
            if (filter != null) {
                result.add(filter);
            }
        }
        return result;
    }

    public static void showHelp(UI ui, String... blocks) {
        Window log = new Window(UI.scale(new Coord(470, 560)), "Filter Help") {
            public void wdgmsg(String msg, Object... args) {
                if(msg.equals("close")) {
                    reqdestroy();
                    return;
                }
                super.wdgmsg(msg, args);
            }
        };
        log.add(new RichTextBox(UI.scale(new Coord(460, 550)), String.join("\n", blocks)), Coord.z);
        log.pack();
        ui.root.add(log, new Coord(100, 40));
    }

    public static class Compound extends ItemFilter {
        private final List<ItemFilter> filters = new LinkedList<>();

        public void add(ItemFilter filter) {
            filters.add(filter);
        }

        public boolean isEmpty() {
            return filters.isEmpty();
        }

        @Override
        public boolean matches(List<ItemInfo> info) {
            if (filters.isEmpty())
                return false;
            for (ItemFilter filter : filters) {
                if (!filter.matches(info)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class Complex extends ItemFilter {
        protected final String text;
        protected final Sign sign;
        protected final Sign opts;
        protected float value;
        protected final boolean all;
        protected final boolean any;

        public Complex(String text, String sign, String value, String opts) {
            this.text = text.toLowerCase();
            this.sign = getSign(sign);
            this.opts = getSign(opts);
            float tmp = 0;
            try {
                tmp = Float.parseFloat(value);
            } catch (Exception ignored) {
            }
            this.value = tmp;

            all = text.equals("*") || text.equals("all");
            any = text.equals("any");
        }

        protected boolean test(double actual) {
            return test(actual, value);
        }

        protected boolean test(double actual, double target) {
            switch (sign) {
                case GREATER:
                    return actual > target;
                case LESS:
                    return actual <= target;
                case EQUAL:
                    return actual == target;
                case GREQUAL:
                    return actual >= target;
                default:
                    return actual >= 0;
            }
        }

        protected Sign getSign(String sign) {
            if (sign == null) {
                return getDefaultSign();
            }
            switch (sign) {
                case ">":
                    return Sign.GREATER;
                case "<":
                    return Sign.LESS;
                case "=":
                    return Sign.EQUAL;
                case "+":
                    return Sign.GREQUAL;
                case "~":
                    return Sign.WAVE;
                default:
                    return getDefaultSign();
            }
        }

        protected Sign getDefaultSign() {
            return Sign.DEFAULT;
        }

        public enum Sign {GREATER, LESS, EQUAL, GREQUAL, WAVE, DEFAULT}
    }

    private static class Text extends ItemFilter {
        private final String text;

        public Text(String text) {
            this.text = text.toLowerCase();
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (item instanceof ItemInfo.Name) {
                String nm = ((ItemInfo.Name) item).original;
                return nm != null && nm.toLowerCase().contains(text);
            }
            return false;
        }
    }

    private static class Has extends Complex {
        public Has(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (item instanceof ItemInfo.Contents) {
                ItemInfo.Contents cnt = (ItemInfo.Contents) item;
                ItemInfo.Contents.Content content = cnt.content();
                if (content != null && content.name != null && content.name.toLowerCase().contains(text)) {
                    return test(content.count);
                }
                if (cnt.sub != null) {
                    for (ItemInfo sub : cnt.sub) {
                        if (sub instanceof ItemInfo.Name) {
                            String nm = ((ItemInfo.Name) sub).original;
                            if (nm != null && nm.toLowerCase().contains(text))
                                return sign == Sign.DEFAULT;
                        }
                    }
                }
            }
            return false;
        }
    }

    private static class XP extends Complex {
        public XP(String text, String sign, String value, String opt) {
            super(text, sign, value, opt);
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (item instanceof Curiosity) {
                Curiosity curio = (Curiosity) item;
                if ("lp".equals(text)) {
                    return test(curio.exp);
                } else if ("xp".equals(text)) {
                    return test(curio.enc);
                } else if ("mw".equals(text)) {
                    return test(curio.mw);
                }
            }
            return false;
        }

        @Override
        protected Sign getDefaultSign() {
            return Sign.GREQUAL;
        }
    }

    private static class Q extends Complex {
        public Q(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        public boolean matches(List<ItemInfo> info) {
            List<QBuff> qs = new ArrayList<>();
            for (ItemInfo ii : info) {
                if (ii instanceof QBuff)
                    qs.add((QBuff) ii);
            }
            if (qs.isEmpty())
                return false;

            // named type (essence/substance/vitality/quality...)
            if (text != null && !text.isEmpty() && !text.equals("single")
                    && !text.equals("min") && !text.equals("max") && !text.equals("average") && !text.equals("avg")) {
                for (QBuff qb : qs) {
                    if (qb.name != null && qb.name.toLowerCase().startsWith(text)) {
                        return test(qb.q);
                    }
                }
                return false;
            }

            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE, sum = 0;
            for (QBuff qb : qs) {
                min = Math.min(min, qb.q);
                max = Math.max(max, qb.q);
                sum += qb.q;
            }
            double avg = sum / qs.size();

            if (text != null && text.startsWith("min")) return test(min);
            if (text != null && text.startsWith("max")) return test(max);
            if (text != null && (text.startsWith("av"))) return test(avg);

            // generic: pick by opt sign
            switch (opts) {
                case GREATER:
                    return test(max);
                case LESS:
                    return test(min);
                default:
                    return test(avg);
            }
        }
    }

    private static class FEP extends Complex {
        public FEP(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (item instanceof FoodInfo) {
                FoodInfo fep = (FoodInfo) item;
                if (text != null && text.length() >= 3) {
                    for (FoodInfo.Event event : fep.evs) {
                        if (event.ev.nm.toLowerCase().startsWith(text)) {
                            return test(event.a);
                        }
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Food extends Complex {
        public Food(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (item instanceof FoodInfo) {
                FoodInfo food = (FoodInfo) item;
                if ("energy".equals(text)) {
                    return test(round(100 * food.end, 2));
                } else if ("hunger".equals(text)) {
                    return test(round(100 * food.glut, 2));
                }
            }
            return false;
        }
    }

    private static class ArmorF extends Complex {
        private static final String[] hard = {"hard", "deflect"};
        private static final String[] soft = {"soft", "soak"};

        private ArmorF(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        private boolean is(String[] names) {
            for (String n : names)
                if (n.startsWith(text))
                    return true;
            return false;
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (item instanceof Armor) {
                Armor armor = (Armor) item;
                if (all || any || text.isEmpty())
                    return test(armor.hard + armor.soft);
                if (is(hard))
                    return test(armor.hard);
                if (is(soft))
                    return test(armor.soft);
                return test(armor.hard + armor.soft);
            }
            return false;
        }
    }

    private static class Gastronomy extends Complex {
        public Gastronomy(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        protected boolean match(ItemInfo item) {
            if (Reflect.is(item, "Gast")) {
                if (text.isEmpty()) {
                    return true;
                }
                if ("fep".startsWith(text)) {
                    return test(round(100D * Reflect.getFieldValueDouble(item, "fev"), 1));
                }
                if ("hunger".startsWith(text)) {
                    return test(round(100D * Reflect.getFieldValueDouble(item, "glut"), 1));
                }
            }
            return false;
        }
    }

    private static class Attribute extends Complex {
        public Attribute(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        public boolean matches(List<ItemInfo> info) {
            Map<haven.res.ui.tt.attrmod.Entry, String> bonuses = ItemInfo.getBonuses(info);
            if (text != null && text.length() >= 3) {
                for (haven.res.ui.tt.attrmod.Entry entry : bonuses.keySet()) {
                    String nm = entry.attr.name();
                    if (nm != null && nm.toLowerCase().startsWith(text)) {
                        double val = (entry instanceof Mod) ? ((Mod) entry).mod : 0;
                        return test(val);
                    }
                }
            } else if (!bonuses.isEmpty()) {
                return true;
            }
            return false;
        }
    }

    private static class Inputs extends Complex {
        public Inputs(String text, String sign, String value, String opts) {
            super(text, sign, value, opts);
        }

        @Override
        public boolean matches(List<ItemInfo> info) {
            Map<Resource, Integer> inputs = ItemInfo.getInputs(info);
            if (text != null && text.length() >= 3) {
                for (Resource res : inputs.keySet()) {
                    try {
                        Resource.Tooltip tt = res.layer(Resource.tooltip);
                        if (tt != null && tt.t.toLowerCase().contains(text)) {
                            return test(inputs.get(res));
                        }
                    } catch (Exception ignored) {
                    }
                    if (res.name.toLowerCase().contains(text)) {
                        return test(inputs.get(res));
                    }
                }
            } else if (!inputs.isEmpty()) {
                return true;
            }
            return false;
        }
    }
}
