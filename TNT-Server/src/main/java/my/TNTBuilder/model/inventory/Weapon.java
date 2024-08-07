package my.TNTBuilder.model.inventory;

import my.TNTBuilder.exception.ValidationException;
import my.TNTBuilder.model.Skill;

import java.util.List;
import java.util.Objects;

public class Weapon extends Item {
    private int meleeRange;
    private int rangedRange;
    private int strength;
    private int reliability;
    private boolean isMasterwork = false;
    private boolean isLargeCaliber = false;
    private boolean hasPrefallAmmo = false;

    //Constructor


    public Weapon(int id, int referenceId, String type, int cost, String specialRules, List<ItemTrait> itemTraits, String rarity, boolean isRelic,
                  int meleeRange, int rangedRange, int strength, int reliability, int handsRequired, String category, boolean isEquipped, Skill grants) {
        super(id, referenceId, type, cost, specialRules, itemTraits, rarity, isRelic, handsRequired, category, isEquipped, grants);
        this.meleeRange = meleeRange;
        this.rangedRange = rangedRange;
        this.strength = strength;
        this.reliability = reliability;
    }

    public Weapon() {
    }

    //Getters

    public void setMeleeRange(int meleeRange) {
        this.meleeRange = meleeRange;
    }

    public void setRangedRange(int rangedRange) {
        this.rangedRange = rangedRange;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setReliability(int reliability) {
        this.reliability = reliability;
    }

    public int getMeleeRange() {
        return meleeRange;
    }

    public int getRangedRange() {
        return rangedRange;
    }

    public int getStrength() {
        return strength;
    }

    public int getReliability() {
        return reliability;
    }

    public boolean isMasterwork() {
        return isMasterwork;
    }

    public void setMasterwork(boolean masterwork) {
        isMasterwork = masterwork;
    }

    public boolean isLargeCaliber() {
        return isLargeCaliber;
    }

    public void setLargeCaliber(boolean largeCaliber)  {
        isLargeCaliber = largeCaliber;
    }

    public boolean isHasPrefallAmmo() {
        return hasPrefallAmmo;
    }

    public void setHasPrefallAmmo(boolean hasPrefallAmmo) {
        this.hasPrefallAmmo = hasPrefallAmmo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Weapon weapon = (Weapon) o;
        return meleeRange == weapon.meleeRange && rangedRange == weapon.rangedRange && strength == weapon.strength
                && reliability == weapon.reliability && isMasterwork == weapon.isMasterwork
                && isLargeCaliber == weapon.isLargeCaliber && hasPrefallAmmo == weapon.hasPrefallAmmo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), meleeRange, rangedRange, strength, reliability, isMasterwork, isLargeCaliber, hasPrefallAmmo);
    }
}