define [], () ->
  # Handle creating new room
  $("#new-public-room").click ->
    $.post "/room/new", (data) ->
      # go to the room
      window.location.href = "/room/" + data.roomId
    .fail ->
      console.log("something went wrong")