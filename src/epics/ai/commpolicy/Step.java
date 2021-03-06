package epics.ai.commpolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epics.common.AbstractAuctionSchedule;
import epics.common.ICameraController;
import epics.common.ITrObjectRepresentation;
import epics.common.RandomNumberGenerator;
import epics.common.RandomUse;
import epics.common.IMessage.MessageType;
import epics.common.AbstractCommunication;

/**
 * Sends the given message to another camera if the link 
 * strength is above a given threshold. 
 * Otherwise communicating with a camera has a very low probability 
 */
public class Step extends AbstractCommunication {

	Broadcast broadcast;
	
	/**
	 * Constructor for Step
	 * @param ai the AI node using the communication policy
	 * @param camController the camera using the AI node and the communication policy
	 */
	public Step(AbstractAuctionSchedule ai, ICameraController camController) {
		super(ai, camController);
		broadcast = new Broadcast(ai, camController);
	}
	
	/**
	 * Multicasts to cameras with a certain linkstrength. if linkstrength is less than a certain threshold, the camera only communicates with a small probability.
	 * @param mt the message type to be communicated
	 * @param o content of the message
	 */
	public void multicast(MessageType mt, Object o) {
		Map<ITrObjectRepresentation, List<String>> advertised = ai.getAdvertisedObjects();
		Map<ITrObjectRepresentation, Integer> stepsTillBroadcast = ai.getStepsTillBroadcast();
		RandomNumberGenerator randomGen = ai.getRandomGen();
		
		if (mt == MessageType.StartSearch) {
			ITrObjectRepresentation io = (ITrObjectRepresentation) o;
			if (AbstractAuctionSchedule.USE_BROADCAST_AS_FAILSAVE) {
				if (!stepsTillBroadcast.containsKey(io)) {
					stepsTillBroadcast.put(io, AbstractAuctionSchedule.STEPS_TILL_BROADCAST);
				}
			}
			
			int sent = 0;
			double ran = randomGen.nextDouble(RandomUse.USE.COMM);
			
			for (ICameraController icc : this.camController.getNeighbours()) {
				String name = icc.getName();
				double prop = 0.1;
				if (ai.vgContainsKey(name, io)) {
					prop = ai.vgGet(name, io);
				}
				if (prop > ran) {
					sent ++;
					ai.incrementSentMessages();
					this.camController.sendMessage(name, mt, o);
					List<String> cams = advertised.get((ITrObjectRepresentation) o);
					if (cams != null) {
						if (!cams.contains(name)) {
							cams.add(name);
						}
					} else {
						cams = new ArrayList<String>();
						cams.add(name);
						advertised.put((ITrObjectRepresentation) o, cams);
					}
				}
			}

			if(sent == 0){
				if(AbstractAuctionSchedule.DEBUG_CAM){
					System.out.println(this.camController.getName() + " tried to MC --> now BC");
				}
				broadcast(mt, o);
			}
		} else {
			if (mt == MessageType.StopSearch) {
				if (advertised.isEmpty()) {
					broadcast(mt, o);
				} else if (advertised.get((ITrObjectRepresentation) o) != null) {
					for (String name : advertised.get((ITrObjectRepresentation) o)) {
						this.camController.sendMessage(name, mt, o);
					}
					advertised.remove(o); 
				}
			}
		}
	}
	
	@Override
	public void broadcast(MessageType mt, Object o) {
		broadcast.multicast(mt, o);
	}
}
