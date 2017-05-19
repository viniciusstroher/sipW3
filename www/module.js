var exec = require('cordova/exec');
var q    = require('q');

var SipW3 = {
    conectarSip:function(user,password,sip) {
        
        var params = {
          sip : sip,
          user: user,
          password  : password
        };

        var defer = Q.defer();

        /*RtspW3 -> nome no plugin.xml*/
        /*abrirRtsp -> metodo*/
        exec(function(suc){
            console.log(suc);
            defer.resolve(suc);
        },function(err){
            console.log(err);
            defer.resolve(err);
        }, "SIP", "conectarSip", [params]);
        
        return defer.promise;
    },
    
    chamar:function(address){
        var params = {address:address};
        exec(null, null, "SIP", "chamar", [params]);
    },

    desconectarSip:function(){
        exec(null, null, "SIP", "desconectarSip", []);
    }


};

module.exports = SipW3;
