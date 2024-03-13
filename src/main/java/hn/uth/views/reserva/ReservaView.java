package hn.uth.views.reserva;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import hn.uth.data.SampleAddress;
import hn.uth.services.SampleAddressService;
import hn.uth.views.MainLayout;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Reserva")
@Route(value = "Reserva/:sampleAddressID?/:action?(edit)", layout = MainLayout.class)
public class ReservaView extends Div implements BeforeEnterObserver {

    private final String SAMPLEADDRESS_ID = "sampleAddressID";
    private final String SAMPLEADDRESS_EDIT_ROUTE_TEMPLATE = "Reserva/%s/edit";

    private final Grid<SampleAddress> grid = new Grid<>(SampleAddress.class, false);

    private TextField ticket;
    private TextField precioTotal;
    private TextField idHabitacion;
    private TextField idCliente;
    private DatePicker fecInicio;
    private DatePicker fecFinal;

    
    private final Button cancel = new Button("Cancelar");
    private final Button save = new Button("Guardar");
    private final Button eliminar = new Button("Eliminar");

    private final BeanValidationBinder<SampleAddress> binder;

    private SampleAddress sampleAddress;

    private final SampleAddressService sampleAddressService;

    public ReservaView(SampleAddressService sampleAddressService) {
        this.sampleAddressService = sampleAddressService;
        addClassNames("reserva-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("ticket").setAutoWidth(true);
        grid.addColumn("precioTotal").setAutoWidth(true);
        grid.addColumn("idHabitacion").setAutoWidth(true);
        grid.addColumn("idCliente").setAutoWidth(true);
        grid.addColumn("fecInicio").setAutoWidth(true);
        grid.addColumn("fecFinal").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEADDRESS_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ReservaView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SampleAddress.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sampleAddress == null) {
                    this.sampleAddress = new SampleAddress();
                }
                
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(ReservaView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } 
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> sampleAddressId = event.getRouteParameters().get(SAMPLEADDRESS_ID).map(Long::parseLong);
        if (sampleAddressId.isPresent()) {
            Optional<SampleAddress> sampleAddressFromBackend = sampleAddressService.get(sampleAddressId.get());
            if (sampleAddressFromBackend.isPresent()) {
                populateForm(sampleAddressFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested sampleAddress was not found, ID = %s", sampleAddressId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ReservaView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        ticket = new TextField("ticket");
        ticket.setPrefixComponent(VaadinIcon.TICKET.create());
        precioTotal = new TextField("precio total");
        precioTotal.setPrefixComponent(VaadinIcon.MONEY.create());
        idHabitacion = new TextField("id habitacion");
        idHabitacion.setPrefixComponent(VaadinIcon.HOME.create());
        idCliente = new TextField("id Cliente");
        idCliente.setPrefixComponent(VaadinIcon.USERS.create());
        fecInicio = new DatePicker("fecha inicio");
        fecInicio.setPrefixComponent(VaadinIcon.CALENDAR.create());
        fecFinal = new DatePicker("fecha final");
        fecFinal.setPrefixComponent(VaadinIcon.CALENDAR.create());
        formLayout.add(ticket, precioTotal, idHabitacion, idCliente, fecInicio, fecFinal);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        eliminar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel, eliminar);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SampleAddress value) {
        this.sampleAddress = value;
        

    }
}
