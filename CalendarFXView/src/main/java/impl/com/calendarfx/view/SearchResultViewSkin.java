/**
 * Copyright (C) 2015, 2016 Dirk Lemmermann Software & Consulting (dlsc.com) 
 * 
 * This file is part of CalendarFX.
 */

package impl.com.calendarfx.view;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.SearchResultView;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@SuppressWarnings("javadoc")
public class SearchResultViewSkin extends SkinBase<SearchResultView> {

    private final ListView<Entry<?>> listView;

    public SearchResultViewSkin(SearchResultView view) {
        super(view);

        Label placeholderLabel = new Label();
        placeholderLabel.getStyleClass().add("placeholder-label"); //$NON-NLS-1$

        listView = new ListView<>();
        listView.setItems(view.getSearchResults());
        listView.setCellFactory(new SearchResultCellFactory());
        listView.setPlaceholder(placeholderLabel);
        listView.getSelectionModel().selectedItemProperty()
                .addListener(it -> view.getProperties().put(
                        "selected.search.result",   //$NON-NLS-1$
                        listView.getSelectionModel().getSelectedItem()));
        getChildren().add(listView);
    }

    public class SearchResultCellFactory
            implements Callback<ListView<Entry<?>>, ListCell<Entry<?>>> {

        @Override
        public ListCell<Entry<?>> call(ListView<Entry<?>> param) {
            return new SearchResultListViewCell();
        }
    }

    public class SearchResultListViewCell extends ListCell<Entry<?>> {

        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        private Circle colorCircle;
        private Label titleLabel;
        private Label dateLabel;
        private Label timeLabel;
        private BorderPane borderPane;

        public SearchResultListViewCell() {
            setPrefWidth(0);

            getStyleClass().add("search-result-cell"); //$NON-NLS-1$

            colorCircle = new Circle();
            colorCircle.setRadius(3.5);

            titleLabel = new Label();
            titleLabel.setMinWidth(0);
            titleLabel.setGraphic(colorCircle);
            titleLabel.getStyleClass().add("title-label"); //$NON-NLS-1$

            dateLabel = new Label();
            dateLabel.setMinWidth(0);
            dateLabel.getStyleClass().add("date-label"); //$NON-NLS-1$

            timeLabel = new Label();
            timeLabel.setMinWidth(0);
            timeLabel.getStyleClass().add("time-label"); //$NON-NLS-1$

            BorderPane dateTimePane = new BorderPane();
            dateTimePane.getStyleClass().add("date-time-pane");
            dateTimePane.setLeft(dateLabel);
            dateTimePane.setRight(timeLabel);

            borderPane = new BorderPane();
            borderPane.getStyleClass().add("container");
            borderPane.setTop(titleLabel);
            borderPane.setBottom(dateTimePane);

            setGraphic(borderPane);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) {
                    Entry<?> entry = getItem();
                    if (entry != null) {
                        fireEvent(new RequestEvent(this, this, entry));
                    }
                }
            });
        }

        @Override
        protected void updateItem(Entry<?> entry, boolean empty) {
            super.updateItem(entry, empty);
            if (entry != null) {
                Calendar calendar = entry.getCalendar();

                borderPane.setVisible(true);
                colorCircle.getStyleClass().add(calendar.getStyle() + "-icon"); //$NON-NLS-1$

                titleLabel.setText(entry.getTitle());
                titleLabel.setVisible(true);
                timeLabel.setText(getTimeText(entry));
                dateLabel.setText(dateFormatter.format(entry.getStartDate()));
            } else {
                borderPane.setVisible(false);
            }
        }

        private String getTimeText(Entry<?> entry) {
            if (entry.isFullDay()) {
                return "all-day"; //$NON-NLS-1$
            }

            LocalDate startDate = entry.getStartDate();
            LocalDate endDate = entry.getEndDate();

            String text;
            if (startDate.equals(endDate)) {
                text = MessageFormat.format(Messages.getString("SearchResultViewSkin.FROM_UNTIL"), //$NON-NLS-1$
                        timeFormatter.format(entry.getStartTime()),
                        timeFormatter.format(entry.getEndTime()));
            } else {
                text = MessageFormat.format(Messages.getString("SearchResultViewSkin.FROM_UNTIL_WITH_DATE"), //$NON-NLS-1$
                        timeFormatter.format(entry.getStartTime()),
                        dateFormatter.format(entry.getStartDate()),
                        timeFormatter.format(entry.getEndTime()),
                        dateFormatter.format(entry.getEndDate()));
            }

            return text;
        }
    }
}
