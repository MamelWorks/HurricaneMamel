package haven;

import java.util.*;
import haven.MenuGrid.Pagina;
import haven.MenuGrid.PagButton;

/* ND: Craft Database search window. Lists every craftable recipe (paginae/craft/*)
 * and lets you filter it either by name (fuzzy) or with ItemFilter queries like
 * "fep:str>10", "attr:agi>2", "q:min<12", "armor:hard>3", "from:board".
 * Modeled on MenuSearch; filter logic ported from Cediner/ArdClient's CraftDBWnd. */
public class CraftDBWnd extends MenuSearch {
    private int pagseq;

    public static final KeyBinding kb_craftdb = KeyBinding.get("craft-db", KeyMatch.forchar('C', KeyMatch.C | KeyMatch.S));

    public CraftDBWnd(MenuGrid menu) {
        super("Craft Database", menu);
        add(new Button(sbox.sz.x, "Filter Help (search tags)", false)
                .action(() -> ItemFilter.showHelp(ui, ItemFilter.FILTER_HELP)),
                sbox.pos("bl").adds(0, 5));
        pagseq = menu.pagseq;
        pack();
    }

    protected boolean generate(List<PagButton> buf) {
        boolean recons = false;
        Collection<Pagina> leaves = new ArrayList<>();
        synchronized(menu.paginae) {
            leaves.addAll(menu.paginae);
        }
        for(Pagina pag : leaves) {
            try {
                Resource res = pag.res();
                if(res != null && res.name.startsWith("paginae/craft/"))
                    buf.add(pag.button());
            } catch(Loading l) {
                recons = true;
            }
        }
        buf.sort(Comparator.comparing(PagButton::name));
        return(recons);
    }

    @Override
    protected void refilter() {
        String q = sbox.text().toLowerCase().trim();
        if(!q.isEmpty() && ItemFilter.isTagQuery(q)) {
            ItemFilter filter = ItemFilter.create(q);
            List<Result> found = new ArrayList<>();
            for(Result r : this.cur) {
                try {
                    List<ItemInfo> info = r.btn.info();
                    if(info != null && filter.matches(info))
                        found.add(r);
                } catch(Loading l) {
                } catch(Exception ignored) {
                }
            }
            found.sort(Comparator.comparing(r -> r.btn.name()));
            this.filtered = found;
            int idx = filtered.indexOf(rls.sel);
            if(idx < 0) {
                if(!filtered.isEmpty()) {
                    rls.change(filtered.get(0));
                    rls.display(0);
                }
            } else {
                rls.display(idx);
            }
        } else {
            super.refilter();
        }
    }

    public void tick(TickEvent ev) {
        if(ev.visible && (pagseq != menu.pagseq)) {
            recons();
            pagseq = menu.pagseq;
        }
        super.tick(ev);
    }
}
