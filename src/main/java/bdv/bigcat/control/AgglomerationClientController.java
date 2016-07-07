/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package bdv.bigcat.control;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;

import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import bdv.bigcat.label.FragmentSegmentAssignment;
import bdv.bigcat.label.IdPicker;
import bdv.labels.labelset.Label;
import bdv.util.AbstractNamedAction;
import bdv.util.AbstractNamedAction.NamedActionAdder;
import bdv.viewer.InputActionBindings;
import bdv.viewer.ViewerPanel;

/**
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Jan Funke &lt;jfunke@iri.upc.edu&gt;
 */
public class AgglomerationClientController
{
	final protected ViewerPanel viewer;
	final protected IdPicker idPicker;
	final protected SelectionController selectionController;
	final protected FragmentSegmentAssignment assignment;
	final protected Socket socket;

	// for behavioUrs
	private final BehaviourMap behaviourMap = new BehaviourMap();
	private final InputTriggerMap inputTriggerMap = new InputTriggerMap();
	private final InputTriggerAdder inputAdder;

	// for keystroke actions
	private final ActionMap ksActionMap = new ActionMap();
	private final InputMap ksInputMap = new InputMap();
	private final NamedActionAdder ksActionAdder = new NamedActionAdder( ksActionMap );
	private final KeyStrokeAdder ksKeyStrokeAdder;

	/* TODO not necessary, just for the record */
	private final List< Action > actions = new LinkedList< Action >();

	static public interface Action
	{
		public String getType();
	}

	static private class Merge implements Action
	{
		final public long[] fragments;

		public Merge( final long[] fragments )
		{
			this.fragments = fragments;
		}

		@Override
		public String getType() { return "merge"; }
	}

	static private class Separate implements Action
	{
		final public long fragment;
		final public long[] from;

		public Separate( final long fragment, final long[] from )
		{
			this.fragment = fragment;
			this.from = from;
		}

		@Override
		public String getType() { return "separate"; }
	}

	static private class ActionSerializer implements JsonSerializer< Action >
	{
		@Override
		public JsonElement serialize(
				Action action,
				Type typeOfAction,
				JsonSerializationContext context )
		{
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty( "type", action.getType() );
			jsonObject.add( "data", context.serialize( action ) );

			return jsonObject;
		}
	}

	public BehaviourMap getBehaviourMap()
	{
		return behaviourMap;
	}

	public InputTriggerMap getInputTriggerMap()
	{
		return inputTriggerMap;
	}

	final GsonBuilder gsonBuilder = new GsonBuilder();
	{
		gsonBuilder
		.registerTypeAdapter(
				FragmentSegmentAssignment.class,
				new FragmentSegmentAssignment.FragmentSegmentSerializer() )
		.registerTypeAdapter(
				Action.class,
				new ActionSerializer() );
		//gsonBuilder.setPrettyPrinting();
	}
	final Gson gson = gsonBuilder.create();

	public AgglomerationClientController(
			final ViewerPanel viewer,
			final IdPicker idPicker,
			final SelectionController selectionController,
			final FragmentSegmentAssignment assignment,
			final ZContext ctx,
			final String solverUrl,
			final InputTriggerConfig config,
			final InputActionBindings inputActionBindings,
			final KeyStrokeAdder.Factory keyProperties )
	{
		this.viewer = viewer;
		this.idPicker = idPicker;
		this.selectionController = selectionController;
		this.assignment = assignment;

		inputAdder = config.inputTriggerAdder( inputTriggerMap, "merge" );
		ksKeyStrokeAdder = keyProperties.keyStrokeAdder( ksInputMap, "merge" );

		/* connect */
		socket = ctx.createSocket( ZMQ.REQ );
		socket.connect( solverUrl );

		new MergeBehaviour( "merge", "shift button1" ).register();
		new SeparateBehavior( "separate", "shift button3" ).register();

		inputActionBindings.addActionMap( "agglomerate", ksActionMap );
		inputActionBindings.addInputMap( "agglomerate", ksInputMap );
	}

	////////////////
	// behavioUrs //
	////////////////

	private abstract class SelfRegisteringBehaviour implements Behaviour
	{
		private final String name;

		private final String[] defaultTriggers;

		public SelfRegisteringBehaviour( final String name, final String ... defaultTriggers )
		{
			this.name = name;
			this.defaultTriggers = defaultTriggers;
		}

		public void register()
		{
			behaviourMap.put( name, this );
			inputAdder.put( name, defaultTriggers );
		}
	}

	private abstract class SelfRegisteringAction extends AbstractNamedAction
	{
		private final String[] defaultTriggers;

		public SelfRegisteringAction( final String name, final String ... defaultTriggers )
		{
			super( name );
			this.defaultTriggers = defaultTriggers;
		}

		public void register()
		{
			ksActionAdder.put( this );
			ksKeyStrokeAdder.put( name(), defaultTriggers );
		}
	}

	private class MergeBehaviour extends SelfRegisteringBehaviour implements ClickBehaviour
	{
		public MergeBehaviour( final String name, final String ... defaultTriggers )
		{
			super( name, defaultTriggers );
		}

		@Override
		public void click( final int x, final int y )
		{
			/* client */
			final long oldActiveFragmentId = selectionController.getActiveFragmentId();
			final long id = idPicker.getIdAtDisplayCoordinate( x, y );
			if ( Label.regular( id ) && Label.regular( oldActiveFragmentId ) )
			{

				assignment.mergeFragmentSegments( oldActiveFragmentId, id );
				selectionController.setActiveFragmentId( id );
				viewer.requestRepaint();

				/* solver */
				final Merge merge = new Merge( new long[]{ oldActiveFragmentId, id } );
				do
				{
					System.out.println( "Sending to " + socket + " :" );
					System.out.println( gson.toJson( merge ) );
				}
				while ( !socket.send( gson.toJson( merge ) ) );

				/* TODO not necessary, just for the record */
				actions.add( merge );
			}
		}
	}

	private class SeparateBehavior extends SelfRegisteringBehaviour implements ClickBehaviour
	{
		public SeparateBehavior( final String name, final String ... defaultTriggers )
		{
			super( name, defaultTriggers );
		}

		@Override
		public void click( final int x, final int y )
		{
			final long id = idPicker.getIdAtDisplayCoordinate( x, y );
			if ( Label.regular( id ) )
			{
				final long oldActiveFragmentId = selectionController.getActiveFragmentId();

				final long sid1 = assignment.getSegment( id );
				final long sid2 = assignment.getSegment( oldActiveFragmentId );
				final long[] from =
						sid1 == sid2 && oldActiveFragmentId != id ?
								new long[]{ oldActiveFragmentId } : new long[ 0 ];

				/* local */
				assignment.detachFragment( id );
				selectionController.setActiveFragmentId( id );
				viewer.requestRepaint();

				/* solver */
				final Separate separate = new Separate( id, from );
				do
				{
					System.out.println( "Sending to " + socket + " :" );
					System.out.println( gson.toJson( separate ) );
				}
				while ( !socket.send( gson.toJson( separate ) ) );

				/* TODO not necessary, just for the record */
				actions.add( separate );
			}
		}
	}
}