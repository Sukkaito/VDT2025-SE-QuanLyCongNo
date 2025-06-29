import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { StaffStatsService } from './services/metric.service';
import { StaffStatDTO, MonthlyStatDTO } from '../../shared/models/staff-stats.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, forkJoin } from 'rxjs';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private staffStatsService = inject(StaffStatsService);

  @ViewChild('stackedColumnChart', { static: true }) stackedColumnChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieChart', { static: true }) pieChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('yearlyChart', { static: true }) yearlyChart!: ElementRef<HTMLCanvasElement>;

  staffStats: StaffStatDTO[] = [];
  monthlyStats: MonthlyStatDTO[] = [];
  limit = 5;
  viewMode: 'month' | 'all-time' = 'month';
  
  private columnChart?: Chart;
  private pieChartInstance?: Chart;
  private yearlyChartInstance?: Chart;
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.loadStaffStats();
    this.loadMonthlyStats();
  }

  ngOnDestroy(): void {
    // Clean up RxJS subscriptions
    this.destroy$.next();
    this.destroy$.complete();
    
    // Destroy Chart.js instances
    this.destroyCharts();
  }

  loadStaffStats(): void {
    const staffStatsRequest = this.viewMode === 'month' 
      ? this.staffStatsService.getTopStaffForMonth(this.limit)
      : this.staffStatsService.getTopStaffAllTime(this.limit);

    staffStatsRequest.pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => {
        if (response.data) {
          this.staffStats = response.data;
          this.updateStaffCharts();
        }
      },
      error: (error) => {
        console.error('Error loading staff stats:', error);
      }
    });
  }

  loadMonthlyStats(): void {
    const monthlyRequests = this.generateLast12MonthsRequests();

    forkJoin(monthlyRequests).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (monthlyStats) => {
        // Transform the monthly responses into MonthlyStatDTO format
        this.monthlyStats = monthlyStats.map((monthData, index) => {
          const date = this.getLast12MonthsDates()[index];
          const contractCount = monthData.contracts.data || 0;
          const invoiceCount = monthData.invoices.data || 0;
          
          return {
            month: date.getMonth() + 1,
            year: date.getFullYear(),
            monthName: date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' }),
            contractCount,
            invoiceCount,
            total: contractCount + invoiceCount
          } as MonthlyStatDTO;
        });
        
        this.updateYearlyChart();
      },
      error: (error) => {
        console.error('Error loading monthly data:', error);
      }
    });
  }

  onViewModeChange(): void {
    this.loadStaffStats();
  }

  onLimitChange(): void {
    this.loadStaffStats();
  }

  private updateStaffCharts(): void {
    // Use setTimeout to ensure DOM is ready
    setTimeout(() => {
      this.updateStackedColumnChart();
      this.updatePieChart();
    }, 0);
  }

  private updateCharts(): void {
    // Use setTimeout to ensure DOM is ready
    setTimeout(() => {
      this.updateStackedColumnChart();
      this.updatePieChart();
      this.updateYearlyChart();
    }, 0);
  }

  private destroyCharts(): void {
    if (this.columnChart) {
      this.columnChart.destroy();
      this.columnChart = undefined;
    }
    if (this.pieChartInstance) {
      this.pieChartInstance.destroy();
      this.pieChartInstance = undefined;
    }
    if (this.yearlyChartInstance) {
      this.yearlyChartInstance.destroy();
      this.yearlyChartInstance = undefined;
    }
  }

  private updateStackedColumnChart(): void {
    if (this.columnChart) {
      this.columnChart.destroy();
    }

    const ctx = this.stackedColumnChart?.nativeElement?.getContext('2d');
    if (!ctx) return;

    const config: ChartConfiguration = {
      type: 'bar' as ChartType,
      data: {
        labels: this.staffStats.map(stat => stat.staffName),
        datasets: [
          {
            label: 'Contracts',
            data: this.staffStats.map(stat => stat.contractCount),
            backgroundColor: 'rgba(54, 162, 235, 0.8)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1
          },
          {
            label: 'Invoices',
            data: this.staffStats.map(stat => stat.invoiceCount),
            backgroundColor: 'rgba(255, 99, 132, 0.8)',
            borderColor: 'rgba(255, 99, 132, 1)',
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          title: {
            display: true,
            text: `Top ${this.limit} Staff - Contracts vs Invoices (${this.viewMode === 'month' ? 'This Month' : 'All Time'})`
          },
          legend: {
            position: 'top'
          },
          tooltip: {
            mode: 'index',
            intersect: false,
            callbacks: {
              title: (tooltipItems) => {
                return tooltipItems[0].label;
              },
              beforeLabel: () => '',
              label: (context) => {
                const dataIndex = context.dataIndex;
                const staffData = this.staffStats[dataIndex];
                
                if (context.datasetIndex === 0) {
                  return `Contracts: ${staffData.contractCount}`;
                } else {
                  return `Invoices: ${staffData.invoiceCount}`;
                }
              },
              afterLabel: (context) => {
                if (context.datasetIndex === 1) {
                  const dataIndex = context.dataIndex;
                  const staffData = this.staffStats[dataIndex];
                  const total = staffData.contractCount + staffData.invoiceCount;
                  return `Total: ${total}`;
                }
                return '';
              },
              labelColor: (context) => {
                return {
                  borderColor: context.dataset.borderColor as string,
                  backgroundColor: context.dataset.backgroundColor as string
                };
              }
            }
          }
        },
        scales: {
          x: {
            stacked: true
          },
          y: {
            stacked: true,
            beginAtZero: true
          }
        }
      }
    };

    this.columnChart = new Chart(ctx, config);
  }

  private updatePieChart(): void {
    if (this.pieChartInstance) {
      this.pieChartInstance.destroy();
    }

    const ctx = this.pieChart?.nativeElement?.getContext('2d');
    if (!ctx) return;

    const totalContracts = this.staffStats.reduce((sum, stat) => sum + stat.contractCount, 0);
    const totalInvoices = this.staffStats.reduce((sum, stat) => sum + stat.invoiceCount, 0);

    const config: ChartConfiguration = {
      type: 'pie' as ChartType,
      data: {
        labels: ['Contracts', 'Invoices'],
        datasets: [{
          data: [totalContracts, totalInvoices],
          backgroundColor: [
            'rgba(54, 162, 235, 0.8)',
            'rgba(255, 99, 132, 0.8)'
          ],
          borderColor: [
            'rgba(54, 162, 235, 1)',
            'rgba(255, 99, 132, 1)'
          ],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: `Total Distribution - Contracts vs Invoices (${this.viewMode === 'month' ? 'This Month' : 'All Time'})`
          },
          legend: {
            position: 'bottom'
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const total = totalContracts + totalInvoices;
                const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : '0';
                return `${context.label}: ${context.parsed} (${percentage}%)`;
              }
            }
          }
        }
      }
    };

    this.pieChartInstance = new Chart(ctx, config);
  }

  private updateYearlyChart(): void {
    if (this.yearlyChartInstance) {
      this.yearlyChartInstance.destroy();
    }

    const ctx = this.yearlyChart?.nativeElement?.getContext('2d');
    if (!ctx) return;

    const config: ChartConfiguration = {
      type: 'bar' as ChartType,
      data: {
        labels: this.monthlyStats.map(stat => stat.monthName),
        datasets: [
          {
            label: 'Contracts',
            data: this.monthlyStats.map(stat => stat.contractCount),
            backgroundColor: 'rgba(54, 162, 235, 0.8)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1
          },
          {
            label: 'Invoices',
            data: this.monthlyStats.map(stat => stat.invoiceCount),
            backgroundColor: 'rgba(255, 99, 132, 0.8)',
            borderColor: 'rgba(255, 99, 132, 1)',
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          title: {
            display: true,
            text: 'Monthly Overview - Last 12 Months'
          },
          legend: {
            position: 'top'
          },
          tooltip: {
            mode: 'index',
            intersect: false,
            callbacks: {
              title: (tooltipItems) => {
                return tooltipItems[0].label;
              },
              beforeLabel: () => '',
              label: (context) => {
                const dataIndex = context.dataIndex;
                const monthData = this.monthlyStats[dataIndex];
                
                if (context.datasetIndex === 0) {
                  return `Contracts: ${monthData.contractCount}`;
                } else {
                  return `Invoices: ${monthData.invoiceCount}`;
                }
              },
              afterLabel: (context) => {
                if (context.datasetIndex === 1) {
                  const dataIndex = context.dataIndex;
                  const monthData = this.monthlyStats[dataIndex];
                  return `Total: ${monthData.total}`;
                }
                return '';
              },
              labelColor: (context) => {
                return {
                  borderColor: context.dataset.borderColor as string,
                  backgroundColor: context.dataset.backgroundColor as string
                };
              }
            }
          }
        },
        scales: {
          x: {
            stacked: true
          },
          y: {
            stacked: true,
            beginAtZero: true
          }
        }
      }
    };

    this.yearlyChartInstance = new Chart(ctx, config);
  }

  private generateLast12MonthsRequests() {
    const monthDates = this.getLast12MonthsDates();
    
    return monthDates.map(date => {
      const startDate = new Date(date.getFullYear(), date.getMonth(), 1);
      const endDate = new Date(date.getFullYear(), date.getMonth() + 1, 0);
      
      const startDateStr = startDate.toISOString().split('T')[0];
      const endDateStr = endDate.toISOString().split('T')[0];
      
      return forkJoin({
        contracts: this.staffStatsService.getTotalContractsForPeriod(startDateStr, endDateStr),
        invoices: this.staffStatsService.getTotalInvoicesForPeriod(startDateStr, endDateStr)
      });
    });
  }

  private getLast12MonthsDates(): Date[] {
    const dates: Date[] = [];
    const currentDate = new Date();
    
    for (let i = 11; i >= 0; i--) {
      const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1);
      dates.push(date);
    }
    
    return dates;
  }
}
