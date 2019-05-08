import Vue from 'vue'
import Vuex from 'vuex'
import VueRouter from 'vue-router'
import VueRx from 'vue-rx'
import * as Rx from 'rxjs/Rx'

import App from './App.vue'

import Routes from './config/routes'

import VuexStore from './main-store'

import './assets/semantic/dist/semantic'
import './assets/semantic/dist/semantic.min.css'

Vue.use(VueRouter)
Vue.use(Vuex)
Vue.use(VueRx, Rx)

Vue.config.productionTip = false

const VuexStoreObj = new Vuex.Store(VuexStore)

const router = new VueRouter({
	routes: Routes
})

// handle 'unconnected instances'
router.beforeEach((to, from, next) => {
	if (to.name === 'login')
		next()

	if (!VuexStoreObj.state.user)
		next({ name: 'login' })
	else
		next()
})

// set user from storage if present
// does not work because JSON.stringify does not strigifies properly...
// const userFromStorage = JSON.parse(window.localStorage.getItem('user'))
// console.log('client loaded from storage: ', userFromStorage)
// if (userFromStorage !== null) {
// 	VuexStoreObj.commit('setUser', userFromStorage)
// }

VuexStoreObj.commit('init')

new Vue({
	router,
	store: VuexStoreObj,
	render: h => h(App)
}).$mount('#app')
