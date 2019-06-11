/**
 * @description Returns Id from session storage if it exists otherwise saves new random Id in session storage & returns it
 * @returns {string}
 */
export function getCurrentTabId() {
	// doesnt work - cannot access tabs in normal JS
	//return browser.tabs.getCurrent().then(tab => tab.id)
	
	const sessionAppIdKey = 'AppId'
	
	const found = sessionStorage.getItem(sessionAppIdKey)
	
	if (!found) {
		const newId = Math.random().toString()
		
		sessionStorage.setItem(sessionAppIdKey, newId)
		
		return newId
	}
	
	return found
}