<template>
    <div>
        <UnitBasics />
        <UnitStats />
        <UnitSkills />
        <UnitInventory />
        <UnitExperience />
    </div>
</template>

<script>
import UnitBasics from './UnitBasics.vue';
import UnitStats from './UnitStats.vue';
import UnitExperience from './UnitExperience.vue';
import UnitSkills from './UnitSkills.vue';
import UnitInventory from './Inventory/UnitInventory.vue';

export default {
    components: {
        UnitBasics,
        UnitStats,
        UnitExperience,
        UnitSkills,
        UnitInventory
    }
}
</script>
