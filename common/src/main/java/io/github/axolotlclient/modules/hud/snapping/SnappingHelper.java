/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.snapping;

import java.util.*;

import com.google.common.collect.Sets;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.render.AxoSprites;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.modules.hud.HudManagerCommon;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.SnapAnchorType;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.BiContainer;
import io.github.axolotlclient.util.ClientColors;
import lombok.Setter;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class SnappingHelper {

	private static final Color LINE_COLOR = ClientColors.SELECTOR_BLUE;
	private final int distance = 4;
	private final HashSet<Integer> x = new HashSet<>();
	private final HashSet<Integer> y = new HashSet<>();
	private final AxoWindow window;
	@Setter
	private Rectangle current;

	public SnappingHelper(List<Rectangle> rects, Rectangle current) {
		rects.forEach(this::addRect);
		this.current = current;
		this.window = AxoWindow.getWindow();
	}

	public static Optional<Integer> getNearby(int pos, Set<Integer> set, int distance) {
		for (Integer integer : set) {
			if (integer - distance <= pos && integer + distance >= pos) {
				return Optional.of(integer);
			}
		}
		return Optional.empty();
	}

	public void addRect(Rectangle rect) {
		x.add(rect.x());
		x.add(rect.xEnd());
		y.add(rect.y());
		y.add(rect.yEnd());
	}

	public void renderSnaps(AxoRenderContext graphics) {
		Optional<Integer> curx, cury;
		if ((curx = getRawXSnap()).isPresent()) {
			int x = curx.get();
			graphics.br$fillRect(x, 0, 1, (int) window.br$getScaledHeight(), LINE_COLOR);
			var isStart = x - distance <= current.x() && x + distance >= current.x();
			if ((isStart && this.x.contains(x + current.width())) || (!isStart && this.x.contains(current.x()))) {
				graphics.br$fillRect(isStart ? x + current.width() : current.x(), 0, 1, (int) window.br$getScaledHeight(), LINE_COLOR);
			}
		}
		if ((cury = getRawYSnap()).isPresent()) {
			int y = cury.get();
			graphics.br$fillRect(0, y, (int) window.br$getScaledWidth(), 1, LINE_COLOR);
			var isStart = y - distance <= current.y() && y + distance >= current.y();
			if ((isStart && this.y.contains(y + current.height())) || (!isStart && this.y.contains(current.y()))) {
				graphics.br$fillRect(0, isStart ? y + current.height() : current.y(), (int) window.br$getScaledWidth(), 1, LINE_COLOR);
			}
		}
		//renderAll();
	}

	public void renderHighlights(AxoRenderContext ctx, HudEntry current) {
		var entries = HudManagerCommon.getInstance().getMoveableEntries();
		for (HudEntry entry : entries) {
			if (entry == current) continue;
			if (entry.dependsOnX(current).isPresent() || entry.dependsOnY(current).isPresent()) continue;
			var touchingX = isTouchingX(current, entry);
			var touchingY = isTouchingY(current, entry);
			var xM = getRawXSnap();
			var yM = getRawYSnap();
			if ((touchingX && xM.isEmpty()) || (touchingY && yM.isEmpty())) return;
			if (touchingX || touchingY) {
				ctx.br$fillRect(entry.getTrueBounds(), ClientColors.SELECTOR_GREEN.withAlpha(100));
			}
		}
	}

	public Optional<Integer> getRawXSnap() {
		Optional<Integer> xSnap = getNearby(current.x, x, distance);
		if (xSnap.isPresent()) {
			return xSnap;
		} else if ((xSnap = getNearby(current.x + current.width, x, distance)).isPresent()) {
			return xSnap;
		} else if ((xSnap = getHalfXSnap()).isPresent()) {
			return xSnap;
		}
		return Optional.empty();
	}

	public Optional<Integer> getRawYSnap() {
		Optional<Integer> ySnap = getNearby(current.y, y, distance);
		if (ySnap.isPresent()) {
			return ySnap;
		} else if ((ySnap = getNearby(current.y + current.height, y, distance)).isPresent()) {
			return ySnap;
		} else if ((ySnap = getHalfYSnap()).isPresent()) {
			return ySnap;
		}
		return Optional.empty();
	}

	public Optional<Integer> getHalfXSnap() {
		int width = (int) (window.br$getScaledWidth() / 2);
		int pos = current.x + Math.round((float) current.width / 2);
		if (width - distance <= pos && width + distance >= pos) {
			return Optional.of(width);
		}
		return Optional.empty();
	}

	public Optional<Integer> getHalfYSnap() {
		int height = (int) (window.br$getScaledHeight() / 2);
		int pos = current.y + Math.round((float) current.height / 2);
		if (height - distance <= pos && height + distance >= pos) {
			return Optional.of(height);
		}
		return Optional.empty();
	}

	@SuppressWarnings("unused")
	public void renderAll(AxoRenderContext graphics) {
		for (Integer xval : x) {
			graphics.br$fillRect(new Rectangle(xval, 0, 1, (int) window.br$getScaledHeight()), ClientColors.WHITE);
		}
		for (Integer yval : y) {
			graphics.br$fillRect(new Rectangle(0, yval, (int) window.br$getScaledWidth(), 1), ClientColors.WHITE);
		}
	}

	public Optional<Integer> getCurrentXSnap() {
		Optional<Integer> xSnap = getNearby(current.x, x, distance);
		if (xSnap.isPresent()) {
			return xSnap;
		} else if ((xSnap = getNearby(current.x + current.width, x, distance)).isPresent()) {
			return xSnap.map(i -> i - current.width());
		} else if ((xSnap = getHalfXSnap()).isPresent()) {
			return xSnap.map(i -> i - (current.width / 2));
		}
		return Optional.empty();
	}

	public Optional<Integer> getCurrentYSnap() {
		Optional<Integer> ySnap = getNearby(current.y, y, distance);
		if (ySnap.isPresent()) {
			return ySnap;
		} else if ((ySnap = getNearby(current.y + current.height, y, distance)).isPresent()) {
			return ySnap.map(i -> i - current.height);
		} else if ((ySnap = getHalfYSnap()).isPresent()) {
			return ySnap.map(i -> i - (current.height / 2));
		}
		return Optional.empty();
	}

	public static void renderLinks(AxoRenderContext graphics, HudEntry current, float lineWidth) {
		var entriesX = new HashMap<>(current.getDependenciesX());
		var entriesY = new HashMap<>(current.getDependenciesY());
		var xEmpty = entriesX.isEmpty();
		var yEmpty = entriesY.isEmpty();
		var cBounds = current.getTrueBounds();
		if (!xEmpty && !yEmpty) {
			var union = Sets.intersection(entriesX.keySet(), entriesY.keySet()).immutableCopy();
			for (HudEntry entry : union) {
				var xType = entriesX.get(entry);
				var yType = entriesY.get(entry);
				int cXMatch = switch (xType) {
					case X_X, X_XEND -> cBounds.x();
					case XEND_X, XEND_XEND -> cBounds.xEnd();
					default -> throw new IllegalArgumentException();
				};
				int cYMatch = switch (yType) {
					case Y_Y, Y_YEND -> cBounds.y();
					case YEND_Y, YEND_YEND -> cBounds.yEnd();
					default -> throw new IllegalArgumentException();
				};
				var eBounds = entry.getTrueBounds();
				int eXMatch = switch (xType) {
					case X_X, XEND_X -> eBounds.x();
					case X_XEND, XEND_XEND -> eBounds.xEnd();
					default -> throw new IllegalArgumentException();
				};
				int eYMatch = switch (yType) {
					case Y_Y, YEND_Y -> eBounds.y();
					case Y_YEND, YEND_YEND -> eBounds.yEnd();
					default -> throw new IllegalArgumentException();
				};
				graphics.br$pushMatrix();
				graphics.br$translateMatrix(cXMatch, cYMatch);
				if (Math.abs(cXMatch - eXMatch) > 2 || Math.abs(cYMatch - eYMatch) > 2) {
					var c1 = ClientColors.SELECTOR_GREEN.toInt();
					var c2 = ClientColors.SELECTOR_RED.toInt();
					graphics.br$fillSegment(0, 0, eXMatch - cXMatch, eYMatch - cYMatch, c1, c2, c2, c2, lineWidth);
				}
				int ang = 0;
				if (cXMatch == cBounds.x() && cYMatch == cBounds.y()) {
					ang = -45;
				} else if (cXMatch == cBounds.x() && cYMatch == cBounds.yEnd()) {
					ang = 45 + 180;
				} else if (cXMatch == cBounds.xEnd() && cYMatch == cBounds.y()) {
					ang = 45;
				} else if (cXMatch == cBounds.xEnd() && cYMatch == cBounds.yEnd()) {
					ang = 180 - 45;
				}
				graphics.br$translateMatrix(-4.5f, -4.5f);
				graphics.br$rotateMatrixAround((float) Math.toRadians(ang), 4.5f, 4.5f);
				graphics.br$drawTexture(AxoSprites.MAGNET_ICON, 0, 0, 9, 9);
				graphics.br$popMatrix();
			}
			union.forEach(e -> {
				entriesX.remove(e);
				entriesY.remove(e);
			});
		}
		if (!xEmpty) {
			for (var entry : entriesX.entrySet()) {
				var xBounds = entry.getKey().getTrueBounds();
				var xType = entry.getValue();
				int x1 = switch (xType) {
					case X_X, X_XEND -> cBounds.x();
					case XEND_X, XEND_XEND -> cBounds.xEnd();
					default -> throw new IllegalArgumentException();
				};
				int x2 = switch (xType) {
					case X_X, XEND_X -> xBounds.x();
					case X_XEND, XEND_XEND -> xBounds.xEnd();
					default -> throw new IllegalArgumentException();
				};
				int y1 = xBounds.y() > cBounds.y() ? cBounds.yEnd() : cBounds.y(), y2 = xBounds.y() > cBounds.y() ? xBounds.y() : xBounds.yEnd();
				graphics.br$pushMatrix();
				var overlap = Math.min(cBounds.yEnd(), xBounds.yEnd()) - Math.max(cBounds.y(), xBounds.y());
				if (overlap <= 0 /*&& Math.abs(y2 - y1) > 2*/) {
					graphics.br$translateMatrix(x1, y1);
					var x = x2 - x1;
					var y = y2 - y1;
					var c1 = ClientColors.SELECTOR_GREEN.toInt();
					var c2 = ClientColors.SELECTOR_RED.toInt();
					graphics.br$fillSegment(0, 0, x, y, c1, c2, c2, c1, lineWidth);
					float ang = (float) Math.atan2(x, y);
					int touchLen = (int) Math.sqrt(x * x + y * y);
					graphics.br$rotateMatrix(-ang);
					graphics.br$translateMatrix(0, touchLen / 2f);
					graphics.br$rotateMatrix(ang);
				} else {
					graphics.br$translateMatrix(x1, Math.max(cBounds.y(), xBounds.y()));
					graphics.br$translateMatrix(0f, overlap / 2f);
				}
				graphics.br$translateMatrix(-4.5f, -4.5f);
				var ang = -90;
				if (x1 == cBounds.xEnd()) {
					ang += 180;
				}
				graphics.br$rotateMatrixAround((float) Math.toRadians(ang), 4.5f, 4.5f);
				graphics.br$drawTexture(AxoSprites.MAGNET_ICON, 0, 0, 9, 9);
				graphics.br$popMatrix();
			}
		}
		if (!yEmpty) {
			for (var entry : entriesY.entrySet()) {
				var yBounds = entry.getKey().getTrueBounds();
				var yType = entry.getValue();
				int y1 = switch (yType) {
					case Y_Y, Y_YEND -> cBounds.y();
					case YEND_Y, YEND_YEND -> cBounds.yEnd();
					default -> throw new IllegalArgumentException();
				};
				int y2 = switch (yType) {
					case Y_Y, YEND_Y -> yBounds.y();
					case Y_YEND, YEND_YEND -> yBounds.yEnd();
					default -> throw new IllegalArgumentException();
				};
				int x1 = yBounds.x() > cBounds.x() ? cBounds.xEnd() : cBounds.x(), x2 = yBounds.x() > cBounds.x() ? yBounds.x() : yBounds.xEnd();
				graphics.br$pushMatrix();
				var overlap = Math.min(cBounds.xEnd(), yBounds.xEnd()) - Math.max(cBounds.x(), yBounds.x());
				if (overlap <= 0 /*|| Math.abs(x2 - x1) > 2*/) {
					graphics.br$translateMatrix(x1, y1);
					var x = x2 - x1;
					var y = y2 - y1;
					var c1 = ClientColors.SELECTOR_GREEN.toInt();
					var c2 = ClientColors.SELECTOR_RED.toInt();
					graphics.br$fillSegment(0, 0, x, y, c1, c1, c2, c2, lineWidth);
					float ang = (float) Math.atan2(x, y);
					int touchLen = (int) Math.sqrt(x * x + y * y);
					graphics.br$rotateMatrix(-ang);
					graphics.br$translateMatrix(0, touchLen / 2f);
					graphics.br$rotateMatrix(ang);
				} else {
					graphics.br$translateMatrix(Math.max(cBounds.x(), yBounds.x()), y1);
					graphics.br$translateMatrix(overlap / 2f, 0f);
				}
				graphics.br$translateMatrix(-4.5f, -4.5f);
				if (y1 == cBounds.yEnd()) {
					graphics.br$rotateMatrixAround((float) Math.PI, 4.5f, 4.5f);
				}
				graphics.br$drawTexture(AxoSprites.MAGNET_ICON, 0, 0, 9, 9);
				graphics.br$popMatrix();
			}
		}
	}

	private static boolean isTouchingY(HudEntry current, HudEntry e) {
		var cur = current.getTrueBounds();
		if (e == current || e == null) return false;
		var bounds = e.getTrueBounds();
		return containsSide(cur.x(), cur.xEnd(), bounds.x(), bounds.xEnd()) && (bounds.y() == cur.y() || bounds.yEnd() == cur.y() || bounds.y() == cur.yEnd() || bounds.yEnd() == cur.yEnd());
	}

	private static boolean isTouchingX(HudEntry current, HudEntry e) {
		var cur = current.getTrueBounds();
		if (e == current || e == null) return false;
		var bounds = e.getTrueBounds();
		return containsSide(cur.y(), cur.yEnd(), bounds.y(), bounds.yEnd()) && (bounds.x() == cur.x() || bounds.xEnd() == cur.x() || bounds.x() == cur.xEnd() || bounds.xEnd() == cur.xEnd());
	}

	private static boolean containsSide(int min1, int max1, int min2, int max2) {
		return (min1 >= min2 && min1 <= max2) || (max1 >= min2 && max1 <= max2) || (min2 >= min1 && max2 <= max1);
	}

	public Collection<BiContainer<HudEntry, SnapAnchorType>> getXTouching(Collection<HudEntry> entries, HudEntry current) {
		var cur = current.getTrueBounds();
		return entries.stream().filter(e -> e != current && isTouchingX(current, e)).map(e -> {
			SnapAnchorType type;
			var bounds = e.getTrueBounds();
			if (cur.x() == bounds.x()) {
				type = SnapAnchorType.X_X;
			} else if (cur.x() == bounds.xEnd()) {
				type = SnapAnchorType.X_XEND;
			} else if (cur.xEnd() == bounds.x()) {
				type = SnapAnchorType.XEND_X;
			} else if (cur.xEnd() == bounds.xEnd()) {
				type = SnapAnchorType.XEND_XEND;
			} else return null;
			return BiContainer.of(e, type);
		}).filter(Objects::nonNull).toList();
	}

	public Collection<BiContainer<HudEntry, SnapAnchorType>> getYTouching(Collection<HudEntry> entries, HudEntry current) {
		var cur = current.getTrueBounds();
		return entries.stream().filter(e -> e != current && isTouchingY(current, e)).map(e -> {
			SnapAnchorType type;
			var bounds = e.getTrueBounds();
			if (cur.y() == bounds.y()) {
				type = SnapAnchorType.Y_Y;
			} else if (cur.y() == bounds.yEnd()) {
				type = SnapAnchorType.Y_YEND;
			} else if (cur.yEnd() == bounds.y()) {
				type = SnapAnchorType.YEND_Y;
			} else if (cur.yEnd() == bounds.yEnd()) {
				type = SnapAnchorType.YEND_YEND;
			} else return null;
			return BiContainer.of(e, type);
		}).filter(Objects::nonNull).toList();
	}

	private static void iterateHudDependencyTree(HudEntry entry, Set<HudEntry> set, Map<HudEntry, Set<HudEntry>> dependencies) {
		set.add(entry);
		if (dependencies.containsKey(entry)) {
			dependencies.get(entry).stream().filter(e -> !set.contains(e)).forEach(e -> iterateHudDependencyTree(e, set, dependencies));
		}
	}

	public static Collection<HudEntry> getNonDependentEntries(HudEntry current, Collection<HudEntry> entries) {
		var reverseDependencies = new HashMap<HudEntry, Set<HudEntry>>();
		for (var e : entries) {
			Sets.union(e.getDependenciesX().keySet(), e.getDependenciesY().keySet())
				.forEach(d -> reverseDependencies.computeIfAbsent(d, unused -> new HashSet<>()).add(e));
		}
		if (!reverseDependencies.containsKey(current)) {
			return entries;
		}

		var depTree = new HashSet<HudEntry>();
		reverseDependencies.get(current).forEach(e -> iterateHudDependencyTree(e, depTree, reverseDependencies));
		var ret = new ArrayList<>(entries);
		ret.removeAll(depTree);
		return ret;
	}
}
