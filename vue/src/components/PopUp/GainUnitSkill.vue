<template>
    <div>
        <h1 class="section-title">Select New Skill</h1>
        <div class="gain-skill-container">
            <div class="skill-purchase-info">
                <div class="skillsets"><strong>Available Skillsets: </strong>
                    <span v-for="(skillset, index) in $store.state.currentUnit.availableSkillsets" :key="skillset.id">
                        {{ index == 0 ? '' : ', ' }}
                        {{ skillset.name }}
                    </span>
                </div>

                <div>
                    <strong>Remaining Skills to Purchase: </strong>{{ $store.state.currentUnit.emptySkills }}
                </div>

                <form>
                    <div class="skill-selection">
                        <strong>Lookup Ability: </strong>

                        <div class="skill-finder">
                            <select class="skill-name" v-model="newSkill">
                                <option selected disabled :value="{}">Choose Ability</option>
                                <option v-for="potentialSkill in potentialSkills" :key="potentialSkill.id"
                                    :value="potentialSkill">
                                    {{ potentialSkill.name }} </option>
                            </select>
                            <p>{{ newSkill.description ? newSkill.description : "---" }}</p>

                        </div>
                    </div>
                </form>

            </div>


            <span class="button-container">
                <button @click="addSkill()">Gain Skill</button>
                <button @click="cancel()">Cancel</button>
            </span>
        </div>



    </div>
</template>

<script>
import UnitService from '../../services/UnitService';

export default {
    data() {
        return {
            potentialSkills: [],
            newSkill: {}
        }
    },
    methods: {
        getPotentialSkills() {
            UnitService.getPotentialSkills(this.$store.state.currentUnit.id)
                .then(response => this.potentialSkills = response.data)
                .catch(error => this.$store.dispatch('showError', error));
        },
        addSkill() {
            UnitService.addSkill(this.$store.state.currentUnit.id, this.newSkill)
                .then(() => {
                    this.$store.dispatch('reloadCurrentUnit');
                    this.cancel();
                })
                .catch(error => this.$store.dispatch('showError', error));
        },
        cancel() {
            this.potentialSkills = [];
            this.newSkill = {};
            this.$store.commit('REMOVE_SHOW_POPUP');
        }
    },
    created() {
        this.getPotentialSkills();
    }
}
</script>


<style scoped>
div.gain-skill-container {
    display: flex;
    flex-direction: column;
    width: 60vw;
    align-items: center;
    justify-content: center;
}

div.skill-purchase-info {
    width: 100%;
    padding: 5px;
    gap: 6px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: start;
    flex-wrap: wrap;
}

div.skillsets {
    width: 100%;
    display: block;
    text-align: start;
}

div.skillsets>span {
    display: inline;
}

div.skill-selection {
    max-width: 100%;
    min-width: 100%
}

div.skill-finder {
    display: flex;
    justify-content: start;
    align-items: center;
    padding-top: 6px;
}

div.skill-finder p {
    padding-left: 6px;
    text-wrap: wrap;
    flex-grow: 1;
    margin: 0px;
}

.skill-name{
    width: 150px;
}

select.skill-name {
    font-size: 1em;
    text-align: center;
}
</style>