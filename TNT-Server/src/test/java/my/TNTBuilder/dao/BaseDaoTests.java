package my.TNTBuilder.dao;

import my.TNTBuilder.model.*;
import my.TNTBuilder.model.inventory.Armor;
import my.TNTBuilder.model.inventory.Equipment;
import my.TNTBuilder.model.inventory.ItemTrait;
import my.TNTBuilder.model.inventory.Weapon;
import my.TNTBuilder.model.userModels.User;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestingDatabaseConfig.class)
public abstract class BaseDaoTests {
    protected static final User USER_1 = new User(1, "user1", "user1", "USER");
    protected static final User USER_2 = new User(2, "user2", "user2", "USER");
    protected static final User USER_3 = new User(3, "user3", "user3", "USER");

    protected static final Armor ARMOR = new Armor(1, 1, "Armor 1", 1, "N/A",
            List.of(new ItemTrait(1, "Trait 1", "Trait 1 Desc")), "N/A", false, 1,
            1, true, 2, 3, 1, "Armor");
    protected static final Weapon WEAPON = new Weapon(2, 5, "Weapon 1", 5, "N/A",
            new ArrayList<>(), "N/A", false, 0, 5, 5, 5,
            1, "Ranged Weapon");
    protected static final Equipment EQUIPMENT = new Equipment(3, 3, "Equipment 1", 3, "N/A",
            new ArrayList<>(), "N/A", false, 0, "Equipment");

    protected static final Weapon RELIC_WEAPON = new Weapon(4, 6, "Relic Weapon", 6,
            "Relic Weapon Desc", new ArrayList<>(), "Rare", true, 6, 0,
            6, 6, 2, "Melee Weapon");

    protected static final Armor TEAM_ARMOR = new Armor(5, 1, "Armor 1", 1, "N/A",
            List.of(new ItemTrait(1, "Trait 1", "Trait 1 Desc")), "N/A", false, 1,
            1, true, 2, 3, 1, "Armor");

    protected static final Weapon TEAM_WEAPON = new Weapon(6, 5, "Weapon 1", 5, "N/A",
            new ArrayList<>(), "N/A", false, 0, 5, 5, 5,
            1, "Ranged Weapon");
    protected static final Equipment TEAM_EQUIPMENT = new Equipment(7, 3, "Equipment 1", 3, "N/A",
            new ArrayList<>(), "N/A", false, 0, "Equipment");

    protected static final Weapon TEAM_RELIC_WEAPON = new Weapon(8, 6, "Relic Weapon", 6,
            "Relic Weapon Desc", new ArrayList<>(), "Rare", true, 6, 0,
            6, 6, 2, "Melee Weapon");


    protected static final Unit UNIT1 = new Unit(1, 1, "UnitName1", "Trade Master", "Leader",
            "Human", 50,10,5,7,6,8,6,5,0,
            "Special rules description",100,0,0,0,
            Arrays.asList(new Skillset(3, "Survival", "Skill"),
                    new Skillset(4, "Quickness", "Skill")),
            new ArrayList<>(), Arrays.asList(ARMOR, WEAPON, EQUIPMENT));
    protected static final Unit UNIT2 = new Unit(2, 3, "UnitName2", "Soldier", "Elite",
            "Mutant", 51,11,6,8,7,9,7,6,1,
            "Special rules description",50,0,0,0,
            List.of(new Skillset(6, "Brawn", "Skill")), new ArrayList<>(), List.of(RELIC_WEAPON));

    protected static final Unit UNIT3 = new Unit(3, 1, "UnitName3", "Class Name", "Specialist",
            "Human", 40,10,5,7,6,8,6,5,0,
            "Special rules description",100,0,0,0,
            List.of(new Skillset(6, "Brawn", "Skill")),
            List.of(new Skill(7, "Bully", "All enemies defeated by this model in" +
                    " close combat are knocked prone in addition to any other combat result.", 6, "Brawn")),
            new ArrayList<>());
    protected final Team TEAM_1 = new Team(1, 1, "Team 1", "Caravanners", 1, 500,
            Arrays.asList(UNIT1, UNIT3), Arrays.asList(TEAM_ARMOR, TEAM_WEAPON, TEAM_EQUIPMENT));
    protected final Team TEAM_2 = new Team(2, 1, "Team 2", "Raiders", 3, 1500,
            new ArrayList<>(), List.of(TEAM_RELIC_WEAPON));
    protected final Team TEAM_3 = new Team(3, 2, "Team 3", "Mutants", 2, 1000,
            Collections.singletonList(UNIT2), new ArrayList<>());
    protected final FactionDTO faction1 = new FactionDTO(1, "Caravanners");



    @Autowired
    protected DataSource dataSource;

    @After
    public void rollback() throws SQLException {
        dataSource.getConnection().rollback();
    }

}
