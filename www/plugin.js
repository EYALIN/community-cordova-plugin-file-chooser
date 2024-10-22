var PLUGIN_NAME = 'FileChooserPlugin';

var FileChooserPlugin = {
    chooseFile: function(options) {
        return new Promise(function (resolve, reject) {
            cordova.exec(resolve, reject, PLUGIN_NAME, 'chooseFile', [options]);
        });
    },

};

module.exports = FileChooserPlugin;
