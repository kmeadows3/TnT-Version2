import { createStore as _createStore } from 'vuex';
import axios from 'axios';
import TeamsService from '../services/TeamsService';
import UnitService from '../services/UnitService';
import { app } from '../main';

export function createStore(currentToken, currentUser) {
  let store = _createStore({
    state: {
      token: currentToken || '',
      user: currentUser || {},
      showTeamList: true,
      showTeamDetail: false,
      showUnitDetail: false,
      showNewUnitForm: false,
      showNewTeamForm: false,
      teamList: [],
      currentTeam: {},
      currentUnit: {},
      showError: false,
      errorMessage: '',
      unitInventoryTraits: [],
      teamInventoryTraits: [],
      unitSkillsSorted:[],
      unitInjuriesSorted:[],
      manageInventory: false,
      showPopup: false,
      showBuyItems: false,
      showGainMoneyForm: false,
      showLoseMoneyForm: false,
      showGainExpForm: false,
      popupSubForm: '',
      currentPage: '',
      itemToModify:'',
      viewNavigation: false
    },
    mutations: {
      SET_AUTH_TOKEN(state, token) {
        state.token = token;
        localStorage.setItem('token', token);
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      },
      SET_USER(state, user) {
        state.user = user;
        localStorage.setItem('user', JSON.stringify(user));
      },
      LOGOUT(state) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        state.token = '';
        state.user = {};
        state.teamList = [];
        state.currentTeam = {};
        state.currentUnit = {};
        state.showUnitDetail=false;
        state.showTeamDetail=false;
        state.currentPage='login';
        axios.defaults.headers.common = {};
      },
      SET_TEAM_LIST(state, teamList) {
        state.teamList = teamList;
      },
      SET_CURRENT_TEAM(state, team) {
        state.currentTeam = team;
        state.manageInventory = false;
        state.showTeamDetail = true;
        state.showTeamList = false;
        state.showUnitDetail = false;
      },
      SET_CURRENT_TEAM_NO_PAGE_CHANGE(state, team){
        state.currentTeam = team;
      },
      CLEAR_CURRENT_TEAM(state) {
        state.currentTeam = {};
        state.currentUnit = {};
        state.manageInventory = false;
        state.showUnitDetail = false;
        state.showTeamDetail = false;
        state.showTeamList = true;
      },
      SET_CURRENT_UNIT(state, unit) {
        state.currentUnit = unit;
        state.showUnitDetail = true;
        state.showTeamDetail = false;
        let unitIndex = state.currentTeam.unitList.findIndex( listUnit => listUnit.id == unit.id);
        state.currentTeam.unitList[unitIndex] = unit;
        store.dispatch('updateUnitInventoryTraits');
        store.dispatch('sortUnitSkills');
        store.dispatch('sortInjuries');
      },
      CLEAR_CURRENT_UNIT(state) {
        store.dispatch('reloadCurrentTeam');
        state.currentUnit = {};
        state.manageInventory = false;
        state.showUnitDetail = false;
        state.showTeamDetail = true;
      },
      TOGGLE_NEW_UNIT_FORM(state) {
        state.showNewUnitForm = !state.showNewUnitForm;
      },
      TOGGLE_NEW_TEAM_FORM(state) {
        state.showNewTeamForm = !state.showNewTeamForm;
      },
      CHANGE_TEAM_NAME(state, newName) {
        state.currentTeam.name = newName;
      },
      GAIN_MONEY(state, addedMoney) {
        state.currentTeam.money += addedMoney;
      },
      LOSE_MONEY(state, lostMoney) {
        if (state.currentTeam.money - lostMoney >= 0) {
          state.currentTeam.money -= lostMoney;
        } else {
          throw "Not enough money for this action";
        }
      },
      CHANGE_UNIT_NAME(state, newName) {
        state.currentUnit.name = newName;
      },
      GAIN_UNSPENT_EXP(state, expToGain) {
        if (expToGain > 0) {
          state.currentUnit.unspentExperience += expToGain;
        } else {
          throw "Experience gained must be positive."
        }
      },
      SHOW_ERROR_OFF(state) {
        state.showError = false;
        state.errorMessage = '';
      },
      SHOW_ERROR_ON(state, newMessage) {
        state.errorMessage = newMessage;
        state.showError = true;
      },
      SET_UNIT_INVENTORY_TRAITS(state, traits) {
        state.unitInventoryTraits = traits;
      },
      SET_MANAGE_INVENTORY(state, value) {
        state.manageInventory = value;
      },
      SET_UNIT_SKILLS_SORTED(state, value){
        state.unitSkillsSorted = value;
      },
      SET_UNIT_INJURIES_SORTED(state, value){
        state.unitInjuriesSorted = value;
      },
      TOGGLE_SHOW_POPUP (state){
        state.showPopup = !state.showPopup;
      },
      REMOVE_SHOW_POPUP(state){
        state.showPopup = false;
        state.showNewTeamForm = false;
        state.showNewUnitForm = false;
        state.showBuyItems = false;
        state.showGainMoneyForm = false;
        state.showLoseMoneyForm = false;
        state.showGainExpForm = false;
        state.popupSubForm = '';
      },
      SET_SHOW_BUY_ITEMS(state, value){
        state.showBuyItems = value;
      },
      SET_SHOW_GAIN_MONEY_FORM(state, value){
        state.showGainMoneyForm = value;
      },
      SET_SHOW_LOSE_MONEY_FORM(state, value){
        state.showLoseMoneyForm = value;
      },
      SET_SHOW_GAIN_EXP_FORM(state, value){
        state.showGainExpForm = value;
      },
      SET_POPUP_SUBFORM(state, value){
        state.popupSubForm = value;
      },
      SET_CURRENT_PAGE(state, value){
        state.currentPage = value;
      },
      SET_ITEM_TO_MODIFY(state, value){
        state.itemToModify = value;
      },
      TOGGLE_VIEW_NAVIGATION(state){
        state.viewNavigation = !state.viewNavigation;
      }

    },
    actions: {
      loadTeams() {
        TeamsService.retrieveTeamList()
          .then(response => {
            store.commit('SET_TEAM_LIST', response.data);
          })
          .catch(err => {
            store.commit('SHOW_ERROR_ON', err.response.data.message)
          });
      },
      reloadCurrentUnit(context) {
        UnitService.getUnit(context.state.currentUnit.id)
          .then(response => {
            store.commit('SET_CURRENT_UNIT', response.data);
            store.dispatch('reloadCurrentTeam');
          })
          .catch(err => store.commit('SHOW_ERROR_ON', err.response.data.message));
      },
      reloadCurrentTeam(context) {
        TeamsService.getTeamById(context.state.currentTeam.id)
          .then(response => {
            store.commit('SET_CURRENT_TEAM_NO_PAGE_CHANGE', response.data);
          })
          .catch(err => store.commit('SHOW_ERROR_ON', err.response.data.message));
      },
      showError(context, error) {
        if (error.response) {
          store.commit('SHOW_ERROR_ON', error.response.data.message);
        } else {
          store.commit('SHOW_ERROR_ON', error);
        }
        store.dispatch('loadTeams');
        if (context.state.currentUnit.id) {
          store.dispatch('reloadCurrentUnit');
        } else if(context.state.currentTeam.id){
          store.dispatch('reloadCurrentTeam');
        }
      },
      updateUnitInventoryTraits(context) {
        let unitInventory = store.state.currentUnit.inventory;
        let unitTraits = [];

        unitInventory.forEach(item => item.itemTraits.forEach(
          trait => {
            if (!unitTraits.some((x) => x.id == trait.id)) {
              unitTraits.push(trait)
            }
          })
        );

        if (store.state.currentUnit.skills.filter(skill => skill.name == "No Legs").length > 0){
          let trait = {
            id: 4,
            name : 'Move or Fire',
            effect: 'Weapon may not be fired if the attacker moved or intends to move during the same activation.'
          }

          let hasMoveOrFireAlready = false;

          unitTraits.forEach( trait => {
            if (trait.name == 'Move or Fire'){
                hasMoveOrFireAlready = true;
            }
          })

          if (!hasMoveOrFireAlready){
            unitTraits.push(trait);
          }
        }

        unitTraits = unitTraits.sort((a, b) => a.name.localeCompare(b.name));

        store.commit('SET_UNIT_INVENTORY_TRAITS', unitTraits);
      },
      sortUnitSkills(context) {
        let unitSkills = store.state.currentUnit.skills;
        let hasHatred = false;
        let hasFrenzied = false;
                        
        store.state.currentUnit.injuries.filter( injury => injury.grants && !unitSkills.includes(injury.grants) ).forEach(injury => {
          injury.grants.addedString = injury.name;
          unitSkills.push(injury.grants);
        });

        store.state.currentUnit.inventory.filter( item => item.equipped && !unitSkills.includes(item.grants)).filter( item => item.grants && item.equipped)
        .forEach( item => {
          item.grants.addedString = item.name;
          unitSkills.push(item.grants);
        });       

        unitSkills.forEach( skill => {
          if (skill.name =="Frenzied" || skill.name == "Frienzied *"){
            hasFrenzied = true;
          } else if (skill.name =="Hatred"){
            hasHatred = true;
          }
        });

        if (hasHatred && !hasFrenzied){
          let frenzied = {
            id: 99,
            name:  'Frenzied *',
            description: 'Only applies when Hatred is active. At the beginning of each activation, model must make an Intelligence test (MET/TN 10). On pass, it acts as normal. On fail, it enters a Frenzy. While in a Frenzy, it may only move or charge towards the nearest enemy model, or make a melee attack if it is already in base contact. It also gets a +2 bonus against Will tests. On subsequent activations, model may test Intelligence (MET/TN 10) to try to return to normal. This test does not consume AP.',
            addedString: '(Hatred)'
          }
          unitSkills.push(frenzied);
        }

        unitSkills = unitSkills.sort((a, b) => a.name.localeCompare(b.name))
        store.commit('SET_UNIT_SKILLS_SORTED', unitSkills);
      },
      sortInjuries(context){
        let unitInjuries = store.state.currentUnit.injuries;
        unitInjuries = unitInjuries.sort((a, b) => a.name.localeCompare(b.name));
        unitInjuries.sort((a,b) => {
          if (a.removable == b.removable){
            return 0;
          } else if ( a.removable){
            return 1;
          }
          return -1;
        });
        store.commit('SET_UNIT_INJURIES_SORTED', unitInjuries);
      }
    }
  });
  return store;
}
