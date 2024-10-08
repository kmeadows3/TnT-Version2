<template>
    <div class="item-container" v-show="weapons.length > 0">
        <div class="item-table">
            <div class="title">Weapons</div>
            <div class="item-list table-label weapon-grid" :class="$store.state.manageInventory ? 'action-mode' : ''">
                <div class="item-name">Type</div>
                <div class="weapon-cost">Cost</div>
                <div class="weapon-range">Range </div>
                <div class="weapon-strength">Strength</div>
                <div class="weapon-reliablity">Reliability</div>
                <div class="weapon-hands">Hands</div>
                <div class="weapon-equip">Equipped</div>
                <div class="item-action weapon-action" v-if="$store.state.manageInventory">
                    Actions</div>
            </div>
            <div class="item-list weapon-grid not-label" v-for="weapon in weapons" :key="'weapon' + weapon.id" :class="$store.state.manageInventory ? 'action-mode' : ''">
                
                <div class="item-name">{{ weapon.name }}</div>
                <div class="weapon-cost">{{ weapon.masterwork || weapon.largeCaliber ? weapon.cost * 2 : weapon.cost }}</div>
                <div class="weapon-range">{{ weapon.rangedString }}</div>
                <div class="weapon-strength">{{ weapon.strengthString }}</div>
                <div class="weapon-reliablity">{{ reliabilityCalculated(weapon) }}</div>
                <div class="weapon-hands">{{ weapon.handsRequired }}</div>
                <div class="item-special-rules weapon-rules">
                    <span v-show="weapon.itemTraits.length == 0 || weapon.specialRules != 'N/A'">
                        {{ weapon.specialRules }}
                        <span v-show="weapon.itemTraits.length > 0">, </span>
                    </span>
                    <span v-show="itemTraitsCalculated(weapon).length > 0">
                        <span v-for="(trait, index) in itemTraitsCalculated(weapon)" :key="'trait' + weapon.id +'-'+ trait.id">
                            <span v-show="index != 0">,</span>
                            {{ trait.name }}</span>
                    </span>
                    <span v-show="weapon.hasPrefallAmmo">, equipped with Pre-Fall Ammo: +1 to ranged stat when fired</span>
                    <span v-show="weapon.masterwork">, gains Balanced: +1 to melee stat when attacking</span>
                </div>
                <div class="item-check weapon-equip">
                    <i class="bi bi-check-circle" title="Currently Equipped" v-show="weapon.equipped"></i>
                    <i class="bi bi-x-circle" title="Currently Unequipped" v-show="!weapon.equipped"></i>
                </div>
                <ItemActions class="item-action weapon-action" :item="weapon"/>
            </div>
        </div>
    </div>
</template>

<script>
import ItemActions from '../../Shared/ItemActions.vue';

export default {
    components: {
        ItemActions
    },
    computed: {
        weapons() {
            let weapons = this.$store.state.currentUnit.inventory.filter(item => item.category != "Armor"  && item.category != "Equipment");
            weapons.forEach((item) => {
                item.rangedString = this.rangedString(item);
                item.strengthString = this.strengthString(item);
                if(item.masterwork && !item.name.includes("Masterwork ")){
                    item.name = "Masterwork " + item.name;
                } else if (item.largeCaliber && !item.name.includes("Large Caliber ")){
                    item.name = "Large Caliber " + item.name;
                }
            });

            weapons.sort((a, b) => {
                const order = {true: 1, false: 2};
                return order[a.equipped] - order[b.equipped] || a.name.localeCompare(b.name)
            })

            return weapons;
        },
        hasMaintainer(){
            return this.$store.state.currentUnit.skills.filter(skill => skill.name == "Maintainer").length > 0;
        },
        hasLongArms(){
            return this.$store.state.currentUnit.skills.filter(skill => skill.name == "Long Arms").length > 0;
        }
    },
    methods: {
        itemTraitsCalculated(weapon) {
            let itemTraits = weapon.itemTraits;
            if (this.$store.state.currentUnit.skills.filter(skill => skill.name == "No Legs").length > 0){
                let trait = {
                    id: 4,
                    name : 'Move or Fire',
                    effect: 'Weapon may not be fired if the attacker moved or intends to move during the same activation.'
                }

                let hasMoveOrFireAlready = false;

                itemTraits.forEach( trait => {
                    if (trait.name == 'Move or Fire'){
                        hasMoveOrFireAlready = true;
                    }
                })

                if (!hasMoveOrFireAlready){
                    itemTraits.push(trait);
                }

            }
            return itemTraits;            
        },
        reliabilityCalculated(weapon){
            if (!this.hasMaintainer || weapon.reliability == 1) {
                return weapon.reliability;
            } else {
                return weapon.reliability - 1;
            }
        },
        rangedString(item){
            let range = '';
            if (item.meleeRange == 0) {
                if (item.name == 'Small Blade') {
                    range = 'Base" / ' + item.rangedRange + '"';
                } else if (item.rangedRange != 0){
                    range = item.rangedRange + '"';
                } else if (item.category == 'Grenade') {
                    range = this.$store.state.currentUnit.strength + '"';
                } else if (this.hasFlamer(item)) {
                    range = "Flamer"
                } else if (this.hasLongArms){
                    range = '1"'
                }else {
                    range = 'Base';
                }
            } else {
                if (item.rangedRange != 0){
                    range = item.meleeRange + '" / ' + item.rangedRange + '"';
                } else {
                    range = item.meleeRange + '"';
                }

            }

            return range;
        },
        strengthString(item){
            let str = '';
            if (item.largeCaliber){
                str = item.strength + 1;
            } else {
                str = item.strength;
            }

            if (item.category == 'Melee Weapon' || item.name == 'Bow' || item.name == 'Compound Bow' || item.name == 'Scorpion Tail' || item.name == 'Crushing Claws'){
                str = 'STR +' + str;
            }

            return str;
        },
        hasFlamer(item){
            let flamerList = item.itemTraits.filter( trait => trait.name.toUpperCase() == "FLAMER TEMPLATE")
            return flamerList.length == 1 ? true : false;
        }
    }
}
</script>

<style scoped>

div.item-list.weapon-grid{
    display: grid;
    grid-template-areas:  "name  cost  range strength reliablity hands equipped"
                          "name  rules rules rules    rules      rules rules   ";
    grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
}

div.item-list.weapon-grid.action-mode{
    display: grid;
    grid-template-areas:  "name  cost  range strength reliablity hands equipped action"
                          "name  rules rules rules    rules      rules rules    rules";
    grid-template-columns: 4fr 2fr 2fr 2fr 2fr 2fr 2fr 3fr;
}


@media only screen and (max-width: 992px) {
    div.item-list.weapon-grid.action-mode{
    grid-template-areas:  "name  cost  range strength reliablity hands equipped"
                          "name  rules rules rules    rules      action action";
    grid-template-columns: 5fr 2fr 2fr 2fr 2fr 2fr 3fr;
    }

    div.table-label.action-mode>div.weapon-action {
        display: none;
    }

    div.item-list>div.weapon-action{
        border-top: dotted var(--thin-border) var(--border-color);;
    }

    div.item-list.action-mode>div.weapon-equip{
        border-right: none;
    }

    div.item-list.action-mode>div.weapon-rules{
        border-right: dotted var(--thin-border) var(--border-color);;
    }

}


@media only screen and (max-width: 600px) {
    div.item-list.weapon-grid.action-mode.not-label{
    grid-template-areas:  "name  cost   range strength reliablity hands equipped"
                          "name   rules rules rules    rules      rules rules"
                          "name action action action    action      action action";
    grid-template-columns: 5fr 2fr 2fr 2fr 2fr 2fr 2fr;
    }

    div.item-list.weapon-grid.action-mode.table-label{
    grid-template-areas:  "name  cost   range strength reliablity hands equipped";
    grid-template-columns: 5fr 2fr 2fr 2fr 2fr 2fr 2fr;
    }

    div.table-label.action-mode>div.weapon-action {
        display: none;
    }

    div.item-list>div.weapon-action{
        border-top: dotted var(--thin-border) var(--border-color);
    }

    div.item-list.action-mode>div.weapon-equip{
        border-right: none;
    }

    div.item-list.action-mode>div.weapon-rules{
        border-right: none;
    }

}


div.item-name{
    grid-area: name;
    height: 100%;
}

div.weapon-cost{
    grid-area: cost;
}
div.weapon-range{
    grid-area: range;
}
div.weapon-strength{
    grid-area: strength;
}
div.weapon-reliability{
    grid-area: reliability;
}
div.weapon-hands{
    grid-area: hands;
}
div.weapon-equipped{
    grid-area: equipped;
}
div.weapon-rules{
    grid-area: rules;
}
div.weapon-action{
    grid-area: action;
}


</style>