<template>
    <div v-if="$store.state.showPopup" class="popup-background">
        <div class="popup">
            <i class="bi bi-x-square" @click ="closePopUp()"></i>
            <NewTeamForm v-if="$store.state.showNewTeamForm"/>
            <NewUnitForm v-if="$store.state.showNewUnitForm"/>
            <BuyItems v-if="$store.state.showBuyItems"/>
            <SimpleForms v-if="showSimpleForm" />
            <AdvanceUnit v-if="$store.state.popupSubForm == 'AdvanceUnit'" />
            <GainUnitSkill v-if="$store.state.popupSubForm == 'GainUnitSkill'" />
            <DeleteUnit v-if="$store.state.popupSubForm == 'DeleteUnit'" />
        </div>
    </div>
</template>


<script>
import NewTeamForm from './NewTeamForm.vue';
import NewUnitForm from './NewUnitForm.vue';
import BuyItems from './BuyItems.vue';
import SimpleForms from './SimpleForms.vue';
import AdvanceUnit from './AdvanceUnit.vue';
import GainUnitSkill from './GainUnitSkill.vue';
import DeleteUnit from './DeleteUnit.vue';

export default {
    methods: {
        closePopUp() {
            this.$store.commit('REMOVE_SHOW_POPUP');
        }
    },
    components: {
        NewTeamForm,
        NewUnitForm,
        BuyItems,
        SimpleForms,
        AdvanceUnit,
        GainUnitSkill,
        DeleteUnit
    },
    computed: {
        showSimpleForm() {
            return this.$store.state.showGainMoneyForm 
                || this.$store.state.showLoseMoneyForm
                || this.$store.state.showGainExpForm;
        }
    }
}
</script>

<style scoped>

div.popup-background{
    position:fixed;
    height: 100vh;
    width: 100vw;
    margin: 0px;
    display: flex;
    top: 0px;
    left: 0px;
    z-index: 5;
    background-color: rgba(0, 0, 0, .2);
    justify-content: center;
    align-items: center;
}

div.popup {
    position: absolute;
    background-color: white;
    border: solid 3px black;
    border-radius: 10px;
    min-width: 33%;
    max-width: 100%;
    max-height: 50%;
    box-shadow: 2px 2px 6px rgba(0, 0, 0, .2);
    padding: 5px 0px;
}

i.bi-x-square {
    position: absolute;
    top: 10px;
    right: 10px;
    border: none;
}



</style>