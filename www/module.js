var exec = require('cordova/exec');

var SipW3 = {
    conectarSip:function(user,password,sip,sucesso,falha) {
        
        var params = {
          sip : sip,
          user: user,
          password  : password
        };

        exec(function(suc){
            console.log(suc);
            sucesso(suc);

        },function(err){
            console.log(err);
            falha(suc);

        }, "SIP", "conectarSip", [params]);
        

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
