
function handleMessage(msgEvent) {
    var messageName = msgEvent.name;
    var messageData = msgEvent.message;
    if (messageName === "babble-kickstart") {
        (function(d, s, id){
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)){ return; }
            js = d.createElement(s); js.id = id;
            js.src = "//ec2-54-200-161-20.us-west-2.compute.amazonaws.com/assets/javascripts/babble.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'babble'));
    }
}

if(window.top == window) {
    safari.self.addEventListener("message", handleMessage, false);
}

