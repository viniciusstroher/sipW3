var exec = require('cordova/exec');

var SipW3 = {
    conectarSip:function(user,password,sip) {
        
        var params = {
          sip : sip,
          user: user,
          password  : password
        };



        /*RtspW3 -> nome no plugin.xml*/
        /*abrirRtsp -> metodo*/
        exec(null, null, "SIP", "conectarSip", [params]);
        
    }
};

module.exports = SipW3;
