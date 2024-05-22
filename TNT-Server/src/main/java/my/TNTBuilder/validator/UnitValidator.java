package my.TNTBuilder.validator;
import my.TNTBuilder.dao.UnitDao;
import my.TNTBuilder.exception.ServiceException;
import my.TNTBuilder.exception.ValidatorException;
import my.TNTBuilder.model.Skill;
import my.TNTBuilder.model.Team;
import my.TNTBuilder.model.Unit;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UnitValidator {

    public static final double MAX_SPECIALIST_RATIO = .34;
    private final int FREELANCER_FACTION_ID = 7;
    UnitDao unitDao;
    public UnitValidator (UnitDao unitDao){
        this.unitDao = unitDao;
    }

    //TODO test everything
    public boolean validFivePointLevel(Unit unit, Unit updatedUnit){
        return validMeleeChange(unit, updatedUnit) || validStrengthChange(unit, updatedUnit)
            || validMoveChange(unit, updatedUnit) || validRangedChange(unit, updatedUnit)
            || validDefenseChange(unit, updatedUnit) || validGainEmptySkill(unit, updatedUnit);
    }

    public boolean validTenPointLevel(Unit unit, Unit updatedUnit){
        return validWoundsChange(unit, updatedUnit) || validPromotion(unit, updatedUnit);
    }

    private boolean unitCanAffordAdvance(Unit unit){
        return unit.getUnspentExperience() - unit.getCostToAdvance() >= 0;
    }

    public void validateUpdatedUnit(Unit updatedUnit, Unit currentUnit){

        if (currentUnit == null){
            throw new ServiceException("Update failed. User does not own unit.");
        }

        if (    !( onlyNameChanged(currentUnit, updatedUnit)
                || onlyUnspentExpGained(currentUnit, updatedUnit)
                || ( validFivePointLevel(currentUnit, updatedUnit) && unitCanAffordAdvance(currentUnit) )
                || ( validTenPointLevel(currentUnit, updatedUnit)  && unitCanAffordAdvance(currentUnit) )
                )
        ) {
            throw new ServiceException("Unit update is not valid");
        }

    }

    public void validateNewClientUnit(Unit unit, Team team) {

        if (team == null) {
            throw new ValidatorException("Invalid Unit. Logged in user does not own team.");
        }

        Unit potentialUnit = unitDao.convertReferenceUnitToUnit(unit.getId());
        if (team.getMoney() - potentialUnit.getBaseCost() < 0){
            throw new ValidatorException("Team cannot afford this unit");
        }

        int unitFaction = unitDao.getFactionIdByUnitReferenceId(unit.getId());
        if (unitFaction != team.getFactionId() && unitFaction != FREELANCER_FACTION_ID) {
            throw new ValidatorException("Invalid unit. Unit does not belong to same faction as team.");
        }

        confirmNewUnitRankIsValidOption(potentialUnit, team);

    }

    public boolean teamMustBuyLeader(Team team){
        return team.getUnitList().stream().noneMatch(teamUnit -> "Leader".equalsIgnoreCase(teamUnit.getRank()));
    }

    public boolean teamCanNotBuyElite(Team team){
        return countRankOccurrence("Elite", team) >= 3;
    }

    public boolean teamCanNotBuySpecialist(Team team){
        int unitCount = team.getUnitList().size();
        int specialistCount = countRankOccurrence("Specialist", team);

        return !(((double) (specialistCount + 1) / unitCount) <= MAX_SPECIALIST_RATIO);
    }

    public boolean teamCanNotBuyFreelancer(Team team){
        int freelancerCount = countRankOccurrence("Freelancer", team);

        return !(team.getBSCost()/200 >= freelancerCount + 1);
    }



    /*
    Private Methods
    */
    private void confirmNewUnitRankIsValidOption(Unit potentialUnit, Team team) {

        if (potentialUnit.getRank().equalsIgnoreCase("Leader") && !teamMustBuyLeader(team)) {
            throw new ValidatorException("Team cannot have two leaders.");
        } else if (potentialUnit.getRank().equalsIgnoreCase("Elite") && teamCanNotBuyElite(team)) {
            throw new ValidatorException("Team cannot take more than 3 elites.");
        } else if (potentialUnit.getRank().equalsIgnoreCase("Specialist") && teamCanNotBuySpecialist(team)) {
            throw new ValidatorException("Specialists may not exceed more than 1/3rd of the team");
        } else if (potentialUnit.getRank().equalsIgnoreCase("Freelancer") && teamCanNotBuyFreelancer(team)) {
            throw new ValidatorException("Team can only have one freelancer per 200 BS cost");
        }else if (teamMustBuyLeader(team) && !potentialUnit.getRank().equalsIgnoreCase("Leader")){
            throw new ValidatorException("Team cannot purchase units until it has a leader");
        }

    }

    private int countRankOccurrence(String rank, Team team) {
        return (int) team.getUnitList().stream()
                .filter( teamUnit -> rank.equalsIgnoreCase( teamUnit.getRank()))
                .count();
    }
    private boolean onlyNameChanged(Unit unit, Unit updatedUnit){
        if ( !(unit.getName().equals(updatedUnit.getName()))) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());

        }
        return false;
    }

    private boolean onlyUnspentExpGained(Unit unit, Unit updatedUnit){
        if ( unit.getUnspentExperience() < updatedUnit.getUnspentExperience()) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());

        }
        return false;
    }

    private boolean validWoundsChange(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getWounds() - unit.getWounds() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }
    private boolean validPromotion(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getMettle() - unit.getMettle() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }

    private boolean validMeleeChange(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getMelee() - unit.getMelee() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }

    private boolean validStrengthChange(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getStrength() - unit.getStrength() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }

    private boolean validMoveChange(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getMove() - unit.getMove() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }

    private boolean validRangedChange(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getRanged() - unit.getRanged() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }

    private boolean validDefenseChange(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getDefense() - unit.getDefense() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getEmptySkills() == updatedUnit.getEmptySkills()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }


    private boolean validGainEmptySkill(Unit unit, Unit updatedUnit){
        if ( updatedUnit.getEmptySkills() - unit.getEmptySkills() == 1) {
            return unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
                    && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
                    && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
                    && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
                    && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
                    && unit.getSpentExperience() == updatedUnit.getSpentExperience()
                    && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
                    && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
                    && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
                    && unit.isBangedUp() == updatedUnit.isBangedUp()
                    && unit.isLongRecovery() == updatedUnit.isLongRecovery()
                    && Objects.equals(unit.getName(), updatedUnit.getName())
                    && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
                    && Objects.equals(unit.getRank(), updatedUnit.getRank())
                    && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
                    && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
                    && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
                    && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
                    && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
                    && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
                    && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
        }
        return false;
    }




    /*
    Completely equal units

    unit.getId() == updatedUnit.getId() && unit.getTeamId() == updatedUnit.getTeamId()
        && unit.getBaseCost() == updatedUnit.getBaseCost() && unit.getWounds() == updatedUnit.getWounds()
        && unit.getDefense() == updatedUnit.getDefense() && unit.getMettle() == updatedUnit.getMettle()
        && unit.getMove() == updatedUnit.getMove() && unit.getRanged() == updatedUnit.getRanged()
        && unit.getMelee() == updatedUnit.getMelee() && unit.getStrength() == updatedUnit.getStrength()
        && unit.getEmptySkills() == updatedUnit.getEmptySkills()
        && unit.getSpentExperience() == updatedUnit.getSpentExperience()
        && unit.getUnspentExperience() == updatedUnit.getUnspentExperience()
        && unit.getTotalAdvances() == updatedUnit.getTotalAdvances()
        && unit.getTenPointAdvances() == updatedUnit.getTenPointAdvances()
        && unit.isBangedUp() == updatedUnit.isBangedUp()
        && unit.isLongRecovery() == updatedUnit.isLongRecovery()
        && Objects.equals(unit.getName(), updatedUnit.getName())
        && Objects.equals(unit.getUnitClass(), updatedUnit.getUnitClass())
        && Objects.equals(unit.getRank(), updatedUnit.getRank())
        && Objects.equals(unit.getSpecies(), updatedUnit.getSpecies())
        && Objects.equals(unit.getSpecialRules(), updatedUnit.getSpecialRules())
        && Objects.equals(unit.getAvailableSkillsets(), updatedUnit.getAvailableSkillsets())
        && new HashSet<>(unit.getSkills()).containsAll(updatedUnit.getSkills())
        && new HashSet<>(updatedUnit.getSkills()).containsAll(unit.getSkills())
        && new HashSet<>(unit.getInventory()).containsAll(updatedUnit.getInventory())
        && new HashSet<>(updatedUnit.getInventory()).containsAll(unit.getInventory());
     */


}
