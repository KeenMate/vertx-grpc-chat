import LoginPage from '../pages/login.vue'
import HomePage from '../pages/home.vue'

import NotFound404 from '../pages/not-found.vue'

export default [
	{
		name: 'login',
		path: '/login',
		component: LoginPage
	},
	{
		name: 'home',
		path: "/",
		component: HomePage
	},
	{
		path: '*',
		redirect: NotFound404
	}
]
