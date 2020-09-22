var exec = require('cordova/exec');

exports.toast = function(arg0, success, error) {
    exec(success, error, "KaraExpressPlugin", "toast", [arg0]);
};
exports.miniDisplay = function(arg0, success, error) {
    exec(success, error, "KaraExpressPlugin", "miniDisplay", arg0);
};
exports.displayString = function(x,y,txt, success, error) {
    exec(success, error, "KaraExpressPlugin", "displayString", [x,y,txt]);
};
exports.readNFC = function(success, error) {
    exec(success, error, "KaraExpressPlugin", "readNFC", []);
};
exports.readMifare = function(success, error) {
    exec(success, error, "KaraExpressPlugin", "readMifare", []);
};
exports.readIMSI = function(success, error) {
    exec(success, error, "KaraExpressPlugin", "readIMSI", []);
};
exports.readPassport = function(isReadPic, success, error) {
    exec(success, error, "KaraExpressPlugin", "readPassport", [isReadPic]);
};
exports.testPrint = function(success, error) {
    exec(success, error, "KaraExpressPlugin", "testPrint", []);
};
exports.print = function(arg0, success, error) {
    exec(success, error, "KaraExpressPlugin", "print", arg0);
};
exports.getText = function(success, error) {
    exec(success, error, "KaraExpressPlugin", "getText", []);
};
exports.scanCode = function(success, error) {
    exec(success, error, "KaraExpressPlugin", "scanCode", []);
};