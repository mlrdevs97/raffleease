import { Routes } from "@angular/router";

export const ADMIN_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./admin-layout.component').then(c => c.AdminLayoutComponent),
        children: [
            { path: '', redirectTo: 'panel', pathMatch: 'full' },    
            { 
                path: 'panel',
                loadComponent: () => import('./panel/panel.component').then(c => c.PanelComponent)
            },
            { 
                path: 'management/:id',
                loadComponent: () => import('./raffle-management/raffle-management.component').then(c => c.RaffleManagementComponent)
            },
            { 
                path: 'create',
                loadComponent: () => import('./raffle-creation/raffle-creation.component').then(c => c.RaffleCreationComponent)
            },
            { 
                path: 'edit/:id',
                loadComponent: () => import('./raffle-edition/raffle-edition.component').then(c => c.RaffleEditionComponent)
            }
        ]
    }
];