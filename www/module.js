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
    
    chamar:function(address,sucesso,falha){
        var params = {address:address};
        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", "chamar", [params]);
    },

    desconectarSip:function(sucesso,falha){
        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", "desconectarSip", []);
    }

    emChamada:function(sucesso,falha){
        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", "emChamada", []);
    }


};

module.exports = SipW3;
