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
    if toBePlayed.length == 1
      trackId = toBePlayed.find(".trackId").val()
      document.title = toBePlayed.find(".title a").text()
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
        track =
          soundCloudId: data.id
          title: data.title
          soundCloudUsername: data.user.username
          soundCloudUsernameUrl: data.user.permalink_url
          permalinkUrl: data.permalink_url
          artworkUrl: data.artwork_url
          duration: data.duration
        window.roomSocket.send(JSON.stringify(track))
    false

  # Receiving things from WebSocket
  receiveEvent = (event) ->
    data = $.parseJSON(event.data)
    if data.membersCount?
      counterTemplate = if (data.membersCount == 1) then "counter-one" else "counter-many"
      counterHtml = Mustache.render(MUSTACHE_TEMPLATES[counterTemplate], data)
      $("#members-count").html(counterHtml)
    else if data.track?
      start = new Date(data.startTime)
      m = start.getMinutes()
      minutes = if (m < 10) then "0" + m else m
      data.startTime = start.getHours() + " : " + minutes
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
