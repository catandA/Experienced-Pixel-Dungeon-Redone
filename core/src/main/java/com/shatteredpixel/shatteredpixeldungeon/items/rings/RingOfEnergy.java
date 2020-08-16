/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2020 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfEnergy extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ENERGY;
	}

	public String statsInfo() {
		if (isIdentified()){
			return Messages.get(this, "stats",
					new DecimalFormat("#.###").format(100f * (1.20f + soloBuffedBonus()*0.5f - 1f)),
					new DecimalFormat("#.###").format(100f * (1.10f + soloBuffedBonus()*0.25f - 1f)));
		} else {
			return Messages.get(this, "typical_stats",
					new DecimalFormat("#.###").format(20f),
					new DecimalFormat("#.###").format(10f));
		}
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Energy();
	}
	
	public static float wandChargeMultiplier( Char target ){
		if (getBuffedBonus(target, Energy.class) > 0) return (float)(1.20f + getBuffedBonus(target, Energy.class)*0.5f);
		return 1f;
	}

	public static float artifactChargeMultiplier( Char target ){
        if (getBuffedBonus(target, Energy.class) > 0) return (float)(1.10f + getBuffedBonus(target, Energy.class)*0.25f);
		return 1f;
	}
	
	public class Energy extends RingBuff {
	}
}
