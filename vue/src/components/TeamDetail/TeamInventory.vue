<template>
    <div class="inventory">
        <div class="item-container">
            <h1 class="section-title">Team Inventory</h1>
            <InventoryActions />
            <div class="item-table team-inventory" v-show="$store.state.currentTeam.inventory.length != 0">
                <div class="table-label item-list">
                    <div class="item-med">Type</div>
                    <div class="item-small">BS Cost</div>
                    <div class="item-large">Special Rules</div>
                    <div class="item-small">Relic</div>
                    <div class="item-small">Support</div>
                    <div class="item-action" v-if="$store.state.manageInventory"> Actions</div>
                </div>
                <div class="item-list" v-for="item in sortedInventory" :key="'teamInventory' + item.id">
                    <div class="item-med">{{ item.name }}</div>
                    <div class="item-small">{{ item.cost }}</div>
                    <div class="item-large item-special-rules">
                        <span v-show="item.itemTraits.length == 0 || item.specialRules != 'N/A'">
                            {{ item.specialRules }}<span v-show="item.itemTraits.length > 0">, </span>
                        </span>
                        <span v-show="item.itemTraits.length > 0">
                            <span v-for="(trait, index) in item.itemTraits" :key="'trait' + item.id + trait.id">
                                <span v-show="index != 0">,</span>
                                {{ trait.name }}</span>
                        </span>
                    </div>
                    <div class="item-small item-check">
                        <i class="bi bi-check-circle" title="Relic" v-show="item.relic"></i>
                        <i class="bi bi-x-circle" v-show="!item.relic"></i>

                    </div>
                    <div class="item-small item-check">
                        <i class="bi bi-check-circle" title="Support" v-show="item.category == 'Support Weapon'"></i>
                        <i class="bi bi-x-circle" v-show="!(item.category == 'Support Weapon')"></i>
                    </div>
                    <ItemActions class="item-action" :item='item' />
                </div>
            </div>
            <div v-show="!$store.state.currentTeam.inventory.length != 0">
                <span>The team has no unassigned items.</span>
            </div>
        </div>


    </div>
</template>

<script>
import InventoryActions from '../InventoryShared/InventoryActions.vue';
import ItemActions from '../InventoryShared/ItemActions.vue';

export default {
    components: {
        InventoryActions,
        ItemActions
    },
    computed: {
        sortedInventory() {
            let inventory = this.$store.state.currentTeam.inventory;
            inventory = inventory.sort((a, b) => a.name.localeCompare(b.name)).sort((a, b) => {
                if (a.relic == true && b.relic == false) {
                    return -1;
                } else if (b.relic == true && a.relic == false) {
                    return 1;
                }

                return 0;
            });
            return inventory;
        }
    }
}
</script>


<style scoped>
div.item-list>.item-small {
    min-width: 75px;
    flex-grow: 1;
    flex-basis: 3%;
    display: flex;
    justify-content: center;
    align-items: center;
}

div.item-list>div.item-action {
    min-width: 220px;
    flex-grow: 1;
    flex-basis: 3%;
    display: flex;
    justify-content: center;
    align-items: center;
}

div.item-list>.item-med {
    min-width: 75px;
    flex-grow: 1;
    flex-basis: 15%;
    align-content: center;
}

div.item-list>.item-large {
    min-width: 150px;
    flex-grow: 5;
    flex-basis: 50%;
}

div.team-inventory {
    margin-top: 10px;
}

div.team-inventory div.item-special-rules {
    border-top: none;
    border-right: dotted 1px black;
}
</style>