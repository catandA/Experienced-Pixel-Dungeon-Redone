/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2020 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.blacksmith;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TormentedSpirit;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.plants.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RegrowthSpiritSprite;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class RegrowingSlasher extends BlacksmithWeapon {
    {
        image = ItemSpriteSheet.GROWTHSWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.1f;

    }

    static final Class<Plant>[] harmfulPlants = new Class[]{Sorrowmoss.class, Stormvine.class, Firebloom.class, Blindweed.class};
    static final Class<Plant>[] helpfulPlants = new Class[]{Earthroot.class, Mageroyal.class, Starflower.class, Sungrass.class};

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        int heal = Math.max(1, attacker.HT / 150);
        ArrayList<Char> affected = new ArrayList<>();
        for (Char ch: Actor.chars()){
            if (ch.alignment == attacker.alignment){
                affected.add(ch);
            }
        }
        for (Char ch: affected){
            int previousLife = ch.HP;
            ch.HP = Math.min(ch.HP + heal, ch.HT);
            if (attacker.fieldOfView[ch.pos]) {
                ch.sprite.emitter().burst(Speck.factory(Speck.HEALING), 2);
                ch.sprite.emitter().burst(LeafParticle.GENERAL, 4);
                if (previousLife != ch.HP)
                    ch.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(heal));
                else {
                    if (Barkskin.currentLevel(attacker) != heal)
                        attacker.sprite.showStatus(0x8e6629, Integer.toString(heal*5));
                    Barkskin.conditionallyAppend( attacker, heal*5, affected.size() );
                }
            }
        }

        float chance = 0.15f + 0.5f * affected.size();
        if (Dungeon.Float() < chance){
            Plant plant = Reflection.newInstance(harmfulPlants[Dungeon.Int(harmfulPlants.length)]);
            if (plant != null) {
                plant.pos = defender.pos;
                plant.activate(defender);
            }
        }
        if (Dungeon.Float() < chance){
            Plant plant = Reflection.newInstance(helpfulPlants[Dungeon.Int(harmfulPlants.length)]);
            if (plant != null) {
                plant.pos = attacker.pos;
                plant.activate(attacker);
            }
        }

        return super.proc(attacker, defender, damage);
    }

    @Override
    public int defenseFactor( Char owner ) {
        return 4;	//4 extra defence
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        beforeAbilityUsed(hero, null);
        hero.sprite.operate(hero.pos, () -> {
            ArrayList<Char> affected = new ArrayList<>();
            for (Char ch: Actor.chars()){
                if (ch.alignment == hero.alignment && ch != hero){
                    affected.add(ch);
                }
            }
            for (Char ch: affected){
                if (hero.fieldOfView[ch.pos]) {
                    ch.damage(ch.HT*2, hero);
                    if (!ch.isAlive()){
                        CellEmitter.get( ch.pos ).burst(LeafParticle.GENERAL, 20);
                        CellEmitter.get( ch.pos ).burst( Speck.factory( Speck.STEAM ), 10 );

                        hero.HP = Math.min(hero.HP + hero.HT / 10, hero.HT);
                        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 10);
                        hero.sprite.emitter().burst(LeafParticle.GENERAL, 8);
                        hero.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(hero.HT / 10));

                        RegrowthSpirit w = new RegrowthSpirit();
                        w.adjustStats( Dungeon.scalingDepth() );
                        w.pos = ch.pos;
                        w.state = w.HUNTING;
                        GameScene.add( w, hero.attackDelay() );
                        Dungeon.level.occupyCell(w);

                        w.sprite.alpha( 0 );
                        w.sprite.parent.add( new AlphaTweener( w.sprite, 1, 0.5f ) );
                    }
                }
            }
            hero.sprite.idle();
            hero.spendAndNext(hero.attackDelay());
            afterAbilityUsed(hero);
        });
    }

    public static class RegrowthSpirit extends TormentedSpirit {

        {
            HP = HT = 3;

            spriteClass = RegrowthSpiritSprite.class;

            alignment = Alignment.ALLY;
        }

        @Override
        public void damage(int dmg, Object src) {
            if (src instanceof Char)
                super.damage(1, src);
        }

        @Override
        public void cleanse() {

        }
    }
}
