import { Injectable } from "@angular/core";
import { Observable, Subject } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ShareUrlsService {

    private eventSource: Subject<void> = new Subject<void>();
    event: Observable<void> = this.eventSource.asObservable();

    updateEvent() {
        this.eventSource.next();
    }

    private urlsSource: Subject<string[]> = new Subject<string[]>();
    urlsUpdates: Observable<string[]> = this.urlsSource.asObservable();

    updateUrls(urls: string[]) {
        this.urlsSource.next(urls);
    }
}
