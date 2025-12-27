package haven.automated;

import haven.*;
import haven.res.ui.tt.q.qbuff.QBuff;
import haven.resutil.WaterTile;

import java.util.List;
import java.util.Map;

public class CheckWaterQuality implements Runnable {
	private static final Coord2d posres = Coord2d.of(0x1.0p-10, 0x1.0p-10).mul(11, 11);
	private GameUI gui;

	public CheckWaterQuality(GameUI gui) {
		this.gui = gui;
	}

	@Override
	public void run() {
		try {
			// Validate tile type
			MCache mcache = gui.ui.sess.glob.map;
			int t = mcache.gettile(gui.map.player().rc.floor(MCache.tilesz));
			Tiler tl = mcache.tiler(t);

			if (!(tl instanceof WaterTile)) {
				gui.ui.error("Must be on water!");
				return;
			}

			Resource res = mcache.tilesetr(t);
			if (res == null || res.name.equals("gfx/tiles/owater") || res.name.equals("gfx/tiles/odeep") || res.name.equals("gfx/tiles/odeeper")) {
				gui.ui.error("Can't check salt water!");
				return;
			}

			// Find container
			WItem container = findWaterContainer();
			if (container == null) {
				gui.ui.error("Need waterskin/waterflask/jug!");
				return;
			}

			// Determine if it's in equipment or inventory
			boolean isInEquipment = false;
			int equipmentSlot = -1;
			Equipory equipory = gui.getequipory();
			if (equipory != null) {
				if (equipory.slots[19] == container) {
					isInEquipment = true;
					equipmentSlot = 19;
				} else if (equipory.slots[20] == container) {
					isInEquipment = true;
					equipmentSlot = 20;
				}
			}

			Coord containerPos = null;
			if (!isInEquipment) {
				Coord calculatedPos = container.c;
				Coord sqsz = Inventory.sqsz;
				containerPos = calculatedPos.sub(1, 1).div(sqsz);
			}

			// Check if container has contents - empty it first to get fresh sample
			ItemInfo.Contents.Content content = getContent(container.item);

			if (content != null && content.count > 0) {
				FlowerMenu.setNextSelection("Empty");
				container.item.wdgmsg("iact", Coord.z, 3);
				Thread.sleep(500);
				FlowerMenu.setNextSelection(null);
			}

			// Take container
			container.item.wdgmsg("take", Coord.z);
			Thread.sleep(100);

			// Fill from ground
			Coord2d playerRc = gui.map.player().rc;
			Coord playerFloor = playerRc.floor(posres);
			gui.map.wdgmsg("itemact", Coord.z, playerFloor, 0);
			Thread.sleep(400);

			// Drop back to original location
			if (isInEquipment) {
				equipory.wdgmsg("drop", equipmentSlot);
			} else {
				gui.maininv.wdgmsg("drop", containerPos);
			}

			// Find container again with retry loop
			WItem filledContainer = null;
			for (int attempt = 0; attempt < 10; attempt++) {
				Thread.sleep(50);
				if (isInEquipment) {
					filledContainer = equipory.slots[equipmentSlot];
				} else {
					filledContainer = findWaterContainerAt(containerPos);
				}
				if (filledContainer != null) {
					break;
				}
			}

			if (filledContainer == null) {
				gui.ui.error("Lost container after filling!");
				return;
			}

			// Extract quality with retry loop
			Double quality = null;
			for (int attempt = 0; attempt < 10; attempt++) {
				quality = extractWaterQuality(filledContainer);
				if (quality != null) {
					break;
				}
				Thread.sleep(100);
			}

			if (quality != null) {
				displayQuality(quality);
			} else {
				gui.ui.error("Couldn't read quality!");
			}

		} catch (InterruptedException e) {
			// Interrupted
		} catch (Exception e) {
			gui.ui.error("Error: " + e.getMessage());
		}
	}

	private WItem findWaterContainer() {
		// Check equipment pouches first (slots 19 and 20)
		Equipory equipory = gui.getequipory();
		if (equipory != null) {
			WItem leftPouch = equipory.slots[19];
			WItem rightPouch = equipory.slots[20];

			if (leftPouch != null) {
				try {
					String resName = leftPouch.item.res.get().name;
					if (resName.equals("gfx/invobjs/small/waterskin") ||
						resName.equals("gfx/invobjs/waterflask") ||
						resName.equals("gfx/invobjs/small/glassjug")) {
						return leftPouch;
					}
				} catch (Loading e) {}
			}

			if (rightPouch != null) {
				try {
					String resName = rightPouch.item.res.get().name;
					if (resName.equals("gfx/invobjs/small/waterskin") ||
						resName.equals("gfx/invobjs/waterflask") ||
						resName.equals("gfx/invobjs/small/glassjug")) {
						return rightPouch;
					}
				} catch (Loading e) {}
			}
		}

		// Check inventory as fallback
		for (Map.Entry<GItem, WItem> entry : gui.maininv.wmap.entrySet()) {
			try {
				String resName = entry.getKey().res.get().name;
				if (resName.equals("gfx/invobjs/waterskin") ||
					resName.equals("gfx/invobjs/waterflask") ||
					resName.equals("gfx/invobjs/glassjug")) {
					return entry.getValue();
				}
			} catch (Loading e) {}
		}

		return null;
	}

	private WItem findWaterContainerAt(Coord indexPos) {
		Coord sqsz = Inventory.sqsz;
		Coord calculatedCoord = indexPos.mul(sqsz).add(1, 1);
		for (Map.Entry<GItem, WItem> entry : gui.maininv.wmap.entrySet()) {
			if (entry.getValue().c.equals(calculatedCoord)) {
				try {
					String resName = entry.getKey().res.get().name;
					if (resName.equals("gfx/invobjs/waterskin") ||
						resName.equals("gfx/invobjs/waterflask") ||
						resName.equals("gfx/invobjs/glassjug")) {
						return entry.getValue();
					}
				} catch (Loading e) {}
			}
		}
		return null;
	}

	private ItemInfo.Contents.Content getContent(GItem item) {
		for (ItemInfo info : item.info()) {
			if (info instanceof ItemInfo.Contents) {
				return ((ItemInfo.Contents) info).content;
			}
		}
		return null;
	}

	private Double extractWaterQuality(WItem container) {
		try {
			List<ItemInfo> infos = container.item.info();
			for (ItemInfo info : infos) {
				if (info instanceof ItemInfo.Contents) {
					ItemInfo.Contents contents = (ItemInfo.Contents) info;
					if (contents.sub != null) {
						for (ItemInfo subInfo : contents.sub) {
							if (subInfo instanceof QBuff) {
								return ((QBuff) subInfo).q;
							}
						}
					}
				}
			}
		} catch (Exception e) {}
		return null;
	}

	private void displayQuality(double quality) {
		String message = String.format("Water Quality: %.1f", quality);
		gui.ui.msg(message);
	}
}
