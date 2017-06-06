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

            addEventListener(document, 'recebeChamadaEvent', function(e) {
              document.body.innerHTML = e.detail;
            });

        },function(err){
            console.log(err);
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

    encerraChamada:function(sucesso,falha){

        exec(function(suc){
            sucesso(suc);
        },function(err){
            falha(err);
        }, "SIP", "encerraChamada", []);
    },





    function addEventListener(el, eventName, handler) {
      if (el.addEventListener) {
        el.addEventListener(eventName, handler);
      } else {
        el.attachEvent('on' + eventName, function() {
          handler.call(el);
        });
      }
    }



};

module.exports = SipW3;
