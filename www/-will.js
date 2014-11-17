    var willPluginLoader(require, exports, module) {

        var exec = require("cordova/exec");

		function WillPlugin()  {

		}


        WillPlugin.prototype.scan = function (successCallback, errorCallback) {
            if (errorCallback == null) {
                errorCallback = function () {
                };
            }

            if (typeof errorCallback != "function") {
                console.log("willPlugin.open failure: failure parameter not a function");
                return;
            }

            if (typeof successCallback != "function") {
                console.log("willPlugin.open failure: success callback parameter must be a function");
                return;
            }

            exec(successCallback, errorCallback, 'willPlugin', 'open', []);
        };

        //-------------------------------------------------------------------

        var willPlugin = new WillPlugin();
        module.exports = willPlugin;

    }

    willPluginLoader(require, exports, module);

    cordova.define("cordova/plugin/WillPlugin", willPluginLoader);