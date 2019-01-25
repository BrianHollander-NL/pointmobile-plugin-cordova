var exec = require('cordova/exec');

var PointMobile = function() {};
PointMobile.getDeviceInformation = function(success, error) {
	exec(success, error, 'PointMobile', 'getDeviceInformation', []);
};
PointMobile.activate = function(success, error) {
	exec(success, error, 'PointMobile', 'MSR_activateReader', []);
};

PointMobile.deactivate = function(success, error) {
	exec(success, error, 'PointMobile', 'MSR_deactivateReader', []);
};

PointMobile.swipe = function (success, error) {
	exec(success, error, 'PointMobile', 'MSR_swipe', []);
};

PointMobile.stopSwipe = function (success, error) {
	exec(success, error, 'PointMobile', 'MSR_stopSwipe', []);
};

PointMobile.startScanner = function (success, error) {
	exec(success, error, 'PointMobile', 'SCAN_activateScanner', []);
};

PointMobile.stopScanner = function (success, error) {
	exec(success, error, 'PointMobile', 'SCAN_deactivateScanner', []);
};

PointMobile.fireEvent = function (event, data) {
	var customEvent = new CustomEvent(event, { 'detail': data} );
	window.dispatchEvent(customEvent);
};

PointMobile.on = function (event, callback, scope) {
	window.addEventListener(event, callback);
};
PointMobile.off = function (event, callback, scope) {
	window.removeEventListener(event, callback);
};
module.exports = PointMobile;

/*
 * Polyfill for adding CustomEvent -- Copy uncommented lines below into your
 * application if you get  Reference Error: CustomEvent is undefined
 * see : https://developer.mozilla.org/fr/docs/Web/API/CustomEvent,
         http://stackoverflow.com/questions/25579986/
	if (!window.CustomEvent) { // Create only if it doesn't exist
	    (function () {
	        function CustomEvent ( event, params ) {
	            params = params || { bubbles: false, cancelable: false, detail: undefined };
	            var evt = document.createEvent( 'CustomEvent' );
	            evt.initCustomEvent( event, params.bubbles, params.cancelable, params.detail );
	            return evt;
	        };

	        CustomEvent.prototype = window.Event.prototype;

	        window.CustomEvent = CustomEvent;
	    })();
	}
*/
