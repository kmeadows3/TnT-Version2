import axios from 'axios';

export default {

  retrieveTeamList() {
    return axios.get('/teams')
  },
  getUnitsForTeam(team) {
    return axios.post(`factions/${team.factionId}`, team);
  },
  buyUnit(newUnit){
    return axios.post('/units', newUnit);
  },
  createNewTeam(newTeam){
    return axios.post('/teams', newTeam);
  },
  getFactionList(team){
    return axios.get('factions');
  },
  updateTeam(team){
    return axios.put(`/teams/${team.id}`, team);
  },
  getPotentialSkills(id){
    return axios.get(`/units/${id}/skills`);
  },
  addSkill(id, skill){
    return axios.post(`/units/${id}/skills`, skill);
  },
  getUnit(id){
    return axios.get(`/units/${id}`);
  },
  updateUnit(unit){
    return axios.put(`/units/${unit.id}`, unit);
  },
  getBaseUnit(unitClass){
    return axios.get(`/units?class=${unitClass}`);
  }

}