// Everything else will be in context to BABBLE
var BABBLE = (function(){
	'use strict';
	
	// constants
	var BABBLE_AUDIO_IDENTIFIER = "owner:BABBLE";
	var BABBLE_CONTENT_IDENTIFIER = "owner:BABBLE";
	var BABBLE_SERVER_SCRIPT_PREFIX = "http://ec2-52-27-159-241.us-west-2.compute.amazonaws.com/utp/";
	var DEBUG = true;
	
	// properties with USER prefix will be available for the end consumer to configure
	var USER_NAVIGATE = true
	
	var paragraphs = [];
	var status = {
		currentParagraphIndex : 0
	};
	
	
	/**
	* logs message to console.log if DEBUG is set to TRUE
	* Additionally it also takes care of prepending log messages with BABBLE
	*/
	
	function log(message, force){
		force = force || false;
		if(DEBUG || force){
			console.log("BABBLE: " + message);
		}
	}
	
	/**
	* Pauses any media playing on the page
	*/
	
	function pauseAllMedia(){
		log("Pausing All Media", true);

		var audios = document.getElementsByTagName("audio");
		var videos = document.getElementsByTagName("video");

		for(var i=0; i<audios.length;i++){
			audios[i].pause();
		}
		
		for(var i=0; i<videos.length;i++){
			videos[i].pause();
		}
	}
	
	/**
	* Highlights the current paragraph based on user preference set into variable USER_NAVIGATE
	*/
	function highlightCurrentParagraph(){
		var paragraph = paragraphs[status.currentParagraphIndex];
		if(USER_NAVIGATE){
			var paragraphElem = document.evaluate(paragraph.xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
			
			var previousParagraphIndex = ((status.currentParagraphIndex - 1) < 0)? paragraphs.length-1: (status.currentParagraphIndex - 1);
			
			// TODO: fade out the previous paragraph
			var previousParagraph = paragraphs[previousParagraphIndex];
			var previousParagraphElem = document.evaluate(previousParagraph.xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
			
			// Rest the previous paragraph
			previousParagraphElem.style.backgroundColor = "";
			
			// set the current paragraph's style
			paragraphElem.style.backgroundColor = "yellow";
		}
		
	}
	
	/**
	* Unhighlights the current paragraph based on user preference set into variable USER_NAVIGATE
	*/
	function unHighlightCurrentParagraph(){
		var paragraph = paragraphs[status.currentParagraphIndex];
		if(USER_NAVIGATE){
			var paragraphElem = document.evaluate(paragraph.xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
			
			// Rest the paragraph
			paragraphElem.style.backgroundColor = "";			
		}
		
	}
	
	/**
	* plays the current paragraph FROM THE START
	* NOTE: this function ensures that only one media (of the current paragraph) is being played at any given point in time
	*/
	
	function playCurrentParagraph(){
		log("playing paragraph no - " + (status.currentParagraphIndex+1));
		
		// stops any other audio playing on the page
		pauseAllMedia();
		
		// plays the audio for the current paragraph
		paragraphs[status.currentParagraphIndex].audioElem.play();
		
		// navigate user to the current paragraph
		highlightCurrentParagraph();
	}
	
	/**
	* does the following things
	* - sets next paragraph as the current paragraph
	* - plays the new paragraph
	* - stops the play if we have reached the end, resets currentParagraphIndex to 0
	*/
	
	function playNextParagraph(){
		// set next paragraph as current paragraph
		status.currentParagraphIndex++;
		
		if(status.currentParagraphIndex >= paragraphs.length){
			// Do clean up
			// unhighlight current paragraph
			unHighlightCurrentParagraph();
			
			status.currentParagraphIndex = status.currentParagraphIndex % paragraphs.length;
				
		} else {
			// FIXME: set this on a timeout
			// play the new paragraph
			playCurrentParagraph();
		}
	}
	
	/**
	* prepares and return and HTML5 audio tag
	*/
	
	function getAudioTag(src){
		var elem = document.createElement("audio");
		elem.setAttribute("autoplay", false);
		elem.setAttribute("src", src);
		
		// this attribute helps us identify that it was added by BABBLE
		elem.setAttribute("data", BABBLE_AUDIO_IDENTIFIER);
		
		// add call back to play the next audio
		elem.addEventListener("ended", function(){
			playNextParagraph();
		})
		
		return elem;
	}
	
	/**
	* Prepares audio for the given paragph. 
	* - creates an audio tag
	* - embed the audio tag to the page, into the corresponding paragraph element
	* - store the reference for the audio element in the paragraph
	
	* Following is the paragraph struct
		{
			"xPath":"value", // xpath to identify the dom element containing the text
			"text":"value", // text value
			"audioURL":"value" // url to read that text out loud
		}
	
	 * @param  {contentMetaDataMap} contentMetaDataMap  Map that contains reading meta data for the current page, here is the structure
	 								 {
									"paragraphs" : [
										{
											"xPath":"value", // xpath to identify the dom element containing the text
											"text":"value", // text value
											"audioURL":"value" // url to read that text out loud
										},
										{..},{..}
									]
	  							}
	 */
	function prepareAudioForParagraph(paragraph){
		var audioElem = getAudioTag(paragraph.audioUrl);
		var paragraphElem = document.evaluate(paragraph.xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
		paragraphElem.appendChild(audioElem);
		paragraph.audioElem = audioElem;
	}
	
	/**
	* set data attribute for the tags modified by BABBLE
	*/
	
	function tagParagraph(paragraph){
		var paragraphElem = document.evaluate(paragraph.xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
		
		// this attribute helps us identify that it was added by BABBLE
		elem.setAttribute("data", BABBLE_CONTENT_IDENTIFIER);
		
	}
	
	// Public Methods

  	/**
	* Initialize BABBLE
	*
   	*/
	
	function initialize(){
		var elem = document.createElement("script");
		var encodedURL = encodeURIComponent(document.URL);
		elem.src = BABBLE_SERVER_SCRIPT_PREFIX + encodedURL;
		document.head.appendChild(elem);
	}
	
	/**
	* Kick starts BABBLE
	* - prepare audio tags for the paragraph and embeds them
	* - plays the first paragraph
	* @param  {contentMetaDataMap} contentMetaDataMap  Map that contains reading meta data for the current page, here is the structure
	 								 {
									"paragraphs" : [
										{
											"xPath":"value", // xpath to identify the dom element containing the text
											"text":"value", // text value
											"audioURL":"value" // url to read that text out loud
										},
										{..},{..}
									]
	  							}
	*/
	
	function kickStart(contentMetaDataMap){
		log("kick started!", true);
		paragraphs = contentMetaDataMap.paragraphs;
		
		// create and embed audio tags into these paragraphs
		for(var i=0;i<paragraphs.length;i++){
			prepareAudioForParagraph(paragraphs[i]);
		}
		
		// play the first paragraph
		playCurrentParagraph();
		
	}
	
	return {
	    init: initialize,
		kickStart: kickStart
	};
	
})();

window.onload = function(){
	BABBLE.init();
};