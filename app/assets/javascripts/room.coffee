define ['scClientId'], (scClientId) ->
  # Check if we shouldn't start playing sth new
  updatePlaylist = () ->
    lastQueuedTrack = $(".queued").last()
    playingAnything = $(".playing").length > 0
    if lastQueuedTrack? and not playingAnything
      lastQueuedTrack.removeClass("queued").addClass("playing")
      playTrackMarkedForPlaying()

  # Play the track
  playTrackMarkedForPlaying = () ->
    toBePlayed = $(".playing")
    if toBePlayed?
      trackId = toBePlayed.find(".trackId").val()
      playTrackById(trackId)

  playTrackById = (trackId) ->
    SC.whenStreamingReady () -> SC.stream(trackId).play
      onfinish: () ->
        $(".playing").removeClass("playing").addClass("played")
        updatePlaylist()

  # User adds new track to the playlist
  $("form.add-track button").click ->
    url = $("form.add-track input").val()
    resolveUrl = "http://api.soundcloud.com/resolve.json?url=" + url + "&client_id=" + scClientId
    $.ajax
      url: resolveUrl
      type: "GET"
      dataType: 'jsonp'
      crossDomain: true
      success: (data) ->
        trackInfo =
          track: data
          playedAt: "15:33"
        window.roomSocket.send(JSON.stringify(trackInfo))
    false

  # Receiving things from WebSocket
  receiveEvent = (event) ->
    data = $.parseJSON(event.data)
    if data.msg?
      console.log(data.msg)
    if data.track?
      trackHtml = Mustache.render(MUSTACHE_TEMPLATES['track'], data)
      $("#playlist").prepend(trackHtml)
      updatePlaylist()

  # Connecting to Room WebSocket
  connectToWs = () ->
    url = "ws://localhost:9000" + window.location.pathname + "/ws"
#    WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    WS = WebSocket
    window.roomSocket = new WS(url)
    window.roomSocket.onmessage = receiveEvent

  # Initialize SounCloud API & bootstrap page
  SC.initialize(
    client_id: scClientId
  )
  playTrackMarkedForPlaying()
  connectToWs()
