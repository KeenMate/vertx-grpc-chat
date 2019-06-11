import {
	BroadcastAddress,
	BroadcastChannelName,
	MasterSlaveDataKey,
	VueWindowsStorageKey
} from '../config/constants.js'
import {getCurrentTabId} from '../common/browser-api.js'

export class BroadcastMessage {
	constructor(address, body) {
		this.address = address
		this.body = body
	}
}

export const initMasterSlaveState = () => ({
	channel: new BroadcastChannel(BroadcastChannelName),
	tabId: -1,
	isInitialized: false,
	isMaster: false,
	sharedState: {
		user: {},
		rooms: [],
		connectedRooms: [],
		currentRoomId: 0,
		users: [],
		messages: []
	}
})

export default {
	data() {
		return {
			masterSlaveData: this.$store.state[MasterSlaveDataKey]
		}
	},
	methods: {
		joinTabsSession() {
			const {masterSlaveData: data} = this
			this.$store.commit('setTabId', getCurrentTabId())
			this.$store.commit('initChannel', {
				onmessage: this.handleBroadcast,
				onerror: (err) => console.error(err)
			})

			this.doEcho()

			setTimeout(() => {
				if (data.isInitialized) {
					return
				}
				// no response in meaningful time

				console.log('Not initialized in specified time (I guess Im Master)')

				this.$store.commit('setIsInitialized', true)
				this.$store.commit('setIsMaster', true)

				this.syncStorage()

				this.onFirstMaster()

				this.allocateResources()

			}, 1000)
		},
		onMasterExists() {
			console.info('You can execute code in this method when this tab is not first: onMasterExists')
		},
		onFirstMaster() {
			console.info('You can execute code in this method when this tab is first: onFirstMaster')
		},
		allocateResources() {
			console.warn('allocateResources method from mixin is not overriden... this is probably not expected')
		},
		freeResources() {
			console.warn('You can free your allocated resources here in this method: freeResources')
		},
		syncStorage() {
			const openedTabs = JSON.parse(localStorage.getItem(VueWindowsStorageKey)) || []

			if (openedTabs.indexOf(this.masterSlaveData.tabId) === -1)
				openedTabs.push(this.masterSlaveData.tabId)

			localStorage.setItem(
				VueWindowsStorageKey,
				JSON.stringify(openedTabs)
			)
		},
		addSlaveToStorage(slaveId) {
			const openedTabs = JSON.parse(localStorage.getItem(VueWindowsStorageKey)) || []

			const index = openedTabs.indexOf(slaveId)
			if (index === -1)
				openedTabs.push(slaveId)

			console.log('Adding Slave to storage')
			localStorage.setItem(VueWindowsStorageKey, JSON.stringify(openedTabs))
		},
		removeSlaveFromStorage(slaveId) {
			let openedTabs = JSON.parse(localStorage.getItem(VueWindowsStorageKey)) || []

			const index = openedTabs.indexOf(slaveId)
			if (index === -1)
				return

			openedTabs.splice(index, 1)

			console.log('Removing Slave from storage')
			localStorage.setItem(VueWindowsStorageKey, JSON.stringify(openedTabs))
		},
		promoteNextSlave() {
			let openedTabs = JSON.parse(localStorage.getItem(VueWindowsStorageKey)) || []

			if (openedTabs.length === 0) {
				console.log('Cannot promote next Master. There is no other slave (dynasty has fallen)')

				return
			}

			openedTabs.splice(0, 1) // openedTabs.indexOf(this.masterSlaveData.tabId)

			localStorage.setItem(
				VueWindowsStorageKey,
				JSON.stringify(openedTabs)
			)

			console.log('promoting next master')
			this.masterSlaveData
			.channel.postMessage(
				new BroadcastMessage(
					BroadcastAddress.NewMaster,
					openedTabs[0]
				)
			)
		},
		doEcho() {
			this.masterSlaveData
			.channel.postMessage(
				new BroadcastMessage(
					BroadcastAddress.Echo,
					this.masterSlaveData.tabId
				)
			)
		},
		onClose() {
			console.log('Im closing')

			if (this.masterSlaveData.isMaster) {
				console.log('And Im master - cleaning resources and promoting next Slave')
				this.freeResources()

				this.promoteNextSlave()
			} else {
				console.log('I was just a Slave - I will tell others about my death')
				this.masterSlaveData
				.channel.postMessage(
					new BroadcastMessage(
						BroadcastAddress.SlaveDied,
						this.masterSlaveData.tabId
					)
				)
			}
		},
		handleBroadcast({data}) {
			switch (data.address) {
				case BroadcastAddress.Echo:
					if (!this.masterSlaveData.isMaster) {
						console.log('Im not master - wont handle it')
						return
					}

					console.log('Im master so I will send my working body & add Slave to storage')
					this.masterSlaveData.channel.postMessage(new BroadcastMessage(BroadcastAddress.EchoEcho, this.masterSlaveData.sharedState))
					this.addSlaveToStorage(data.body)
					break
				case BroadcastAddress.EchoEcho:
					if (this.masterSlaveData.isInitialized)
						return

					console.log('It is echo echo message and now Im not initialized - Im not master now')

					this.$store.dispatch('setMasterSlaveData', {
						isMaster: false,
						isInitialized: true,
						sharedState: data.body
					}).then(() => {
						this.onMasterExists(this.masterSlaveData)
					})
					break
				case BroadcastAddress.SlaveDied:
					if (!this.masterSlaveData.isMaster)
						return

					console.log('It is Slave died message & Im master so I will handle it')

					this.removeSlaveFromStorage(data.body)
					break
				case BroadcastAddress.NewMaster:
					if (data.body !== this.masterSlaveData.tabId)
						return

					console.log('Its Next master & its me.. Im now new Master!!')

					this.$store.commit('setIsMaster', true)

					// setup resources (stream hooks etc.)
					this.allocateResources()
					break
				default:
					let result
					if (this.handleCustomMessage && typeof this.handleCustomMessage === 'function')
						result = this.handleCustomMessage(data)

					if (result === undefined)
						console.warn('Custom message handler didnt return anything.. (return 0 for OK or 1 for FAIL)')
					else if (result === 1)
						console.warn(`Broadcast message with address: '${data.address}' cannot be parsed`)
					break
			}
		}
	}
}
