import toastr from 'toastr'
import 'toastr/build/toastr.min.css'

toastr.options = {
	"closeButton": true,
	"debug": false,
	"positionClass": "toast-top-center",
	"onclick": null,
	"showDuration": "15000",
	"hideDuration": "10000",
	"timeOut": "3000",
	"extendedTimeOut": "1000",
	"showEasing": "swing",
	"hideEasing": "linear",
	"showMethod": "fadeIn",
	"hideMethod": "fadeOut"
}

export function toastInfo(title, msg) {
	toastr.info(msg, title)
}

export function toastWarning(title, msg) {
	toastr.warn(msg, title)
}

export function toastError(title, msg) {
	toastr.error(msg, title)
}

export function toastSuccess(title, msg) {
	toastr.success(msg, title)
}
