package pokerBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import pokerEnums.eGame;
import pokerEnums.eRank;
import pokerEnums.eSuit;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlElement;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

public class PlayHand {
	private static SessionFactory factory;
	
	public static void main(String[] args) throws Exception {

		try{

	          factory = new AnnotationConfiguration().
	                  configure().
	                  //addPackage("com.xyz") //add package if used.
	                  addAnnotatedClass(Hand.class).
	                  buildSessionFactory();
		
		 }catch (Throwable ex) { 
	         System.err.println("Failed to create sessionFactory object." + ex);
	         throw new ExceptionInInitializerError(ex); 
	      }
	          
		PlayJokerWild();
	}


	private static void PlayJokerWild() throws Exception {

		Session session = factory.openSession();	
		Transaction tx = null;
		
		try{
	         tx = session.beginTransaction();
	         tx.commit();
	         
		}catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	         session.close();
		
		
		
		// Table is created
		Table tbl = new Table();

		// Rule set is called (A given game has a rule set)
		Rule rle = new Rule(eGame.DeucesWild);

		// Game is created (tables have players, players play games, etc)
		GamePlay gme = new GamePlay(rle);
		tbl.AddGame(gme);

		// Deck is created using game's ruleset
		Deck d = new Deck(gme.GetNumberOfJokers(), rle.GetRuleCards());

		Map PlayerMap = new HashMap();
		
		// Add the players to Table and Game, give them empty hands
		for (int i = 0; i < gme.GetMaxNumberOfPlayers(); i++) {
			Player p = new Player(null);
			switch (i) {
			case 0:
				p.SetPlayerName("Bob");
				break;
			case 1:
				p.SetPlayerName("Jim");
				break;
			case 2:
				p.SetPlayerName("Joe");
				break;
			case 3:
				p.SetPlayerName("Jane");
				break;				
			}
			PlayerMap.put(p.GetPlayerID(), p);			
			p.SetPlayerNbr(i + 1);
			tbl.AddTablePlayer(p);
			gme.AddGamePlayer(p);
			p.SetHand(new Hand());
		}

		// Five Card in each hand
		for (int i = 0; i < gme.GetNumberOfCards(); i++) {
			for (Player p : gme.GetGamePlayers()) {
				Hand h = p.GetHand();
				h.setPlayerID(p.GetPlayerID());
				h.AddCardToHand(d.drawFromDeck());
				p.SetHand(h);
			}
		}

		// Handle jokers
		for (Player p : gme.GetGamePlayers()) {
			Hand h = p.GetHand();
			h.HandleJokerWilds();
			p.SetHand(h);
		}

		// Collect All Hands in a Game
		ArrayList<Hand> AllHands = new ArrayList<Hand>();
		for (Player p : gme.GetGamePlayers()) {
			AllHands.add(p.GetHand());
		}

		// Find the best hand between players
		Collections.sort(AllHands, Hand.HandRank);
		
		//	For Lab #5...  for each "h", write it to the database.
		// 	I want ONLY the following elements saved for each record:
		//  playerID;
		//	HandStrength;
		//  Natural
		//	HiHand;
		//	LoHand;
		//	Kicker;
		for (Player p : gme.GetGamePlayers()) {
			Hand h = p.GetHand();
			session.save(h);
		}
		
		Player WinningPlayer = (Player) PlayerMap.get(AllHands.get(0).getPlayerID());
		System.out.println(WinningPlayer.GetPlayerName().toString() + " is the winner!!");	
		System.out.println(AllHands.get(0).getHandStrength());

		for (Card c : AllHands.get(0).getCards()) {
			System.out.print(c.getRank());
			System.out.print(" ");
			System.out.print(c.getSuit());
			System.out.print("      ");
		}

		System.out.println("");
		System.out.println("");
		System.out.println("");

		/*
		 * StringWriter swTable = tbl.SerializeMe();
		 * System.out.println("*******   Table XML   *******");
		 * System.out.println(swTable.toString());
		 * 
		 * StringWriter swDeck = d.SerializeMe();
		 * System.out.println("*******   Deck XML   *******");
		 * System.out.println(swDeck.toString());
		 */
	      }
	}
}


