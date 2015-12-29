
function handleMessage(msgEvent) {
    var messageName = msgEvent.name;
    var messageData = msgEvent.message;
    if (messageName === "babble-kickstart") {
        (function(d, s, id){
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)){ return; }
            js = d.createElement(s); js.id = id;
            js.src = "//localhost:9000/assets/javascripts/babble.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'babble'));
    }
}

if(window.top == window) {
    safari.self.addEventListener("message", handleMessage, false);
}

