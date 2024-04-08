package com.skeletor.plugin.javascript.communication.incoming.audio;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.skeletor.plugin.javascript.audio.RoomAudioManager;
import com.skeletor.plugin.javascript.audio.RoomPlaylist;
import com.skeletor.plugin.javascript.communication.incoming.IncomingWebMessage;
import com.skeletor.plugin.javascript.communication.outgoing.OutgoingWebMessage;
import com.skeletor.plugin.javascript.communication.outgoing.audio.PlaySongComposer;
import com.skeletor.plugin.javascript.override_packets.outgoing.JavascriptCallbackComposer;

public class NextSongEvent extends IncomingWebMessage<NextSongEvent.JSONNextSongEvent> {
  public NextSongEvent() {
    super(JSONNextSongEvent.class);
  }
  
  public void handle(GameClient client, JSONNextSongEvent message) {
    Room currentRoom = client.getHabbo().getHabboInfo().getCurrentRoom();
    if (currentRoom == null)
      return; 
    if (currentRoom.hasRights(client.getHabbo())) {
      RoomPlaylist playlist = RoomAudioManager.getInstance().getPlaylistForRoom(currentRoom.getId());
      playlist.nextSong();
      playlist.setPlaying(true);
      PlaySongComposer playSongComposer = new PlaySongComposer(playlist.getCurrentIndex());
      currentRoom.sendComposer((new JavascriptCallbackComposer((OutgoingWebMessage)playSongComposer)).compose());
      currentRoom.sendComposer(playlist.getNowPlayingBubbleAlert().compose());
    } 
  }
  
  public static class JSONNextSongEvent {
    public int currentIndex;
  }
}
