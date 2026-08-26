/*
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

package io.github.axolotlclient.oldanimations.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.SnowGolemEntity;
import net.minecraft.entity.living.mob.monster.*;
import net.minecraft.entity.living.mob.monster.boss.WitherEntity;
import net.minecraft.entity.living.mob.passive.animal.ChickenEntity;
import net.minecraft.entity.living.mob.passive.animal.tameable.OcelotEntity;

public class MobUtil {
	public static MobUtil INSTANCE = new MobUtil();

	/* 14w06a changed mob hitbox sizes */

	public float oldMobWidth(Entity entity, float width) {
		if (entity instanceof SnowGolemEntity && width == 0.7F) {
			return 0.4F;
		}
		if (entity instanceof SilverfishEntity && width == 0.4F) {
			return 0.3F;
		}
		if (entity instanceof SlimeEntity) {
			/* the width is dependent on the scale */
			/* so we can't really check the width easily */
			return width / 0.51000005F * 0.6F;
		}
		if (entity instanceof ChickenEntity && width == 0.4F) {
			return 0.3F;
		}
		return width;
	}

	public float oldMobHeight(Entity entity, float height) {
		if (entity instanceof SnowGolemEntity && height == 1.9F) {
			return 1.8F;
		}
		if (entity instanceof SilverfishEntity && height == 0.3F) {
			return 0.7F;
		}
		if (entity instanceof SlimeEntity) {
			/* the height is dependent on the scale */
			/* so we can't really check the height easily */
			return height / 0.51000005F * 0.6F;
		}
		if (entity instanceof WitherEntity && height == 3.5F) {
			return 4.0F;
		}
		if (entity instanceof OcelotEntity && height == 0.7F) {
			return 0.8F;
		}
		if ((entity instanceof ZombieEntity || entity instanceof WitchEntity || entity instanceof SkeletonEntity) &&
			height == 1.95F) {
			return 1.8F;
		}
		if (entity instanceof SkeletonEntity && height == 2.535F) {
			/* wither skeleton */
			return 2.34F;
		}
		return height;
	}
}
