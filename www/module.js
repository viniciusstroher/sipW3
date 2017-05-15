var exec = require('cordova/exec');

var SipW3 = {
    conectarSip:function(sip,pass,ip) {
        
        var params = {
          sip: sip,
          pass: pass,
          ip: ip
        };



        /*RtspW3 -> nome no plugin.xml*/
        /*abrirRtsp -> metodo*/
        exec(null, null, "SIP", "conectarSip", [params]);
        
    }
};

module.exports = SipW3;
