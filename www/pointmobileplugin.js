// Empty constructor
function PointMobilePlugin() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
PM66Plugin.prototype.show = function(message, duration, successCallback, errorCallback) {
  var options = {};
  options.message = message;
  options.duration = duration;
  cordova.exec(successCallback, errorCallback, 'PointMobilePlugin', 'show', [options]);
}

// Installation constructor that binds ToastyPlugin to window
PointMobilePlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.pointmobilePlugin = new PointMobilePlugin();
  return window.plugins.pointmobilePlugin;
};
cordova.addConstructor(PointMobilePlugin.install);