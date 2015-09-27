(function(d, s, id){
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)){ return; }
    js = d.createElement(s); js.id = id;
    js.src = "//ec2-52-27-159-241.us-west-2.compute.amazonaws.com/assets/javascripts/babble.js";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'babble'));
