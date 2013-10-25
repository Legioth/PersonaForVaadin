window.org_vaadin_leif_persona_Persona = function() {
	var self = this;
	console.log("init");
	
	window[this.getState().domId] = {
		request: function() {
			var state = self.getState();
			state.oncancel = self.getRpcProxy().oncancel;
			console.log("window.navigator.id.request", state);
			window.navigator.id.request(state);
		},
		logout: function() {
			window.navigator.id.logout();
		}
	}
	
	this.watch = function(loggedInUser) {
		var rpc = this.getRpcProxy();
		var watchSettings = {
			onlogin: rpc.onlogin,
			onlogout: rpc.onlogout,
			onready: rpc.onready,
//			onmatch: function() {
//				console.log("onmatch", arguments);
//			},
			loggedInUser: loggedInUser
		};
		console.log('window.navigator.id.watch', watchSettings);
		window.navigator.id.watch(watchSettings);
	}
	
}