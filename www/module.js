var exec = require('cordova/exec');

var SipW3 = {
    //navigator.SIP.conectarSip(1060,"password","192.168.0.43",function(r){console.log('s',r)},function(r){console.log('e',r)});

    conectarSip:function(user,password,sip,sucesso,falha) {
        
        var params = {
          sip : sip,
          user: user,
          password  : password
        };

        exec(function(suc){
            sucesso(suc);

        },function(err){
            falha(err);

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
    },

    emChamada:function(sucesso,falha){
        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", "emChamada", []);
    },

    toogleSpeaker:function(opt,sucesso,falha){
        if(opt === undefined){
            opt = 1;
        }
        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", opt == 1 ? "toogleSpeakerRecebeLigacao" : "toogleSpeakerEnviaLigacao", []);
    },

    encerraChamada : function(sucesso,falha){

        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", "encerraChamada", []);
    },

    recebeEvento:function(){
        console.log("Iniciando recebeEvento")
        document.addEventListener('recebeEvento', function (e) { 
            console.log('recebeEvento',e);
        }, false);
    },


    enviaDadoEventos:function(dado){
        var event = new Event('recebeEvento',{dado : dado});
        document.dispatchEvent(event);

    }
};

module.exports = SipW3;
