package hn.uth.controller;

import hn.uth.data.ReservaResponse;
import hn.uth.model.DatabaseRepositoryImpl;
import hn.uth.views.reserva.ViewModelReserva;
public class InteractorImplReserva implements InteractorReserva{

	private DatabaseRepositoryImpl modelo;
	private ViewModelReserva vista;
	
	public InteractorImplReserva(ViewModelReserva view) {
		super();
		this.vista = view;
		this.modelo = DatabaseRepositoryImpl.getInstance("https://apex.oracle.com", 30000L);
	}

	@Override
	public void consultarReserva() {
		// TODO Auto-generated method stub
		try {
			ReservaResponse respuesta = this.modelo.consultarReserva();
			if(respuesta == null ||respuesta.getCount() == 0 || respuesta.getItems() == null) {
				this.vista.mostrarMensajeError("No hay Cliente que mostrar");
				
			}else {
				this.vista.mostrarReservaEnGrid(respuesta.getItems());
			}
			
		}catch (Exception error) {
			error.printStackTrace();
		}
		
	}
}
