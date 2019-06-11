import {toastError} from './toastr.js'

export function toastGrpcNetError(action) {
	toastError('Network error', `Could not contact remote server to: ${action}`)
}